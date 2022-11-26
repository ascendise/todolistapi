package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.checklist.*
import ch.ascendise.todolistapi.task.*
import ch.ascendise.todolistapi.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

internal class ChecklistTaskControllerTest {

    private lateinit var controller: ChecklistTaskController

    private val checklistTaskService = mockk<ChecklistTaskService>()
    private val checklistTaskModelAssembler = ChecklistTaskModelAssembler()
    private val checklistModelAssembler = ChecklistModelAssembler(TaskModelAssembler())

    private val user = User(id = 101, username = "Nico Nussmueller", subject = "auth|120104")

    @BeforeEach
    fun setUp() {
        controller = ChecklistTaskController(checklistTaskService,
            checklistTaskModelAssembler,
            checklistModelAssembler)
    }

    @Test
    fun `should return relations between tasks and checklists`() {
        every { checklistTaskService.getRelations(101) } returns listOf(
            ChecklistTask(301, 201, 101),
            ChecklistTask(301, 202, 101)
        )
        val response = controller.getRelations(user)
        val expectedRelations = listOf(
            ChecklistTaskResponseDto(301, 201),
            ChecklistTaskResponseDto(301, 202)
        )
        expectedRelations.forEach { addExpectedLinks(it) }
        val expectedResponse = CollectionModel.of(expectedRelations,
            linkTo<ChecklistTaskController> { getRelations(user) }.withSelfRel(),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations")
        )
        assertEquals(expectedResponse, response)
        verify { checklistTaskService.getRelations(101) }
    }

    @Test
    fun `should add relation and return the updated checklist`() {
        val addRequest = ChecklistTaskRequestDto(301, 201)
        every {
            checklistTaskService.addTask(addRequest.toChecklistTask(user))
        } returns Checklist(name = "My Checklist", id = 301, user = user,
            tasks = mutableListOf(Task(id = 201, name = "My task", user = user))
        )
        val result = controller.addRelation(user, addRequest)
        val expectedResponse = ChecklistResponseDto(id = 301, name = "My Checklist", mutableListOf(
            TaskResponseDto(id = 201, name = "My task")
        ))
        addExpectedLinks(expectedResponse)
        assertEquals(expectedResponse, result)
    }

    private fun addExpectedLinks(dto: ChecklistTaskResponseDto): ChecklistTaskResponseDto {
        val dummyUser = User(id = 101, username = "", subject = "")
        return dto.add(
            linkTo<ChecklistController> { getChecklist(dto.checklistId, dummyUser) }.withRel("checklist"),
            linkTo<TaskController> { getTask(dummyUser, dto.taskId) }.withRel("task"),
            linkTo<ChecklistTaskController> { removeRelation(dummyUser, dto.checklistId, dto.taskId) }.withRel("removeTask"),
            linkTo<ChecklistTaskController> { getRelations(dummyUser) }.withRel("relations")
        )
    }

    private fun addExpectedLinks(dto: ChecklistResponseDto) {
        dto.add(
            linkTo<ChecklistController> { getChecklist(dto.id, user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations")
        )
        dto.tasks.forEach { addExpectedLinks(it) }
    }

    private fun addExpectedLinks(dto: TaskResponseDto) {
        dto.add(
            linkTo<TaskController> { getTask(user, dto.id) }.withSelfRel(),
            linkTo<TaskController> { getTasks(user) }.withRel("tasks")
        )
    }

    @Test
    fun `should throw TaskNotFoundException if task does not exist`() {
        val addRequest = ChecklistTaskRequestDto(checklistId = 301, taskId = 9999)
        every { checklistTaskService.addTask(addRequest.toChecklistTask(user)) } throws TaskNotFoundException()
        assertThrows<TaskNotFoundException> { controller.addRelation(user, addRequest) }
        verify { checklistTaskService.addTask(addRequest.toChecklistTask(user)) }
    }

    @Test
    fun `should throw ChecklistNotFoundException if task does not exist`() {
        val addRequest = ChecklistTaskRequestDto(checklistId = 99999, taskId = 201)
        every { checklistTaskService.addTask(addRequest.toChecklistTask(user)) } throws ChecklistNotFoundException()
        assertThrows<ChecklistNotFoundException> { controller.addRelation(user, addRequest) }
        verify { checklistTaskService.addTask(addRequest.toChecklistTask(user)) }
    }

    @Test
    fun `should remove relation between checklist and task`() {
        val checklistTask = ChecklistTask(301, 201, 101)
        val passedChecklistTask = slot<ChecklistTask>()
        every { checklistTaskService.removeTask(capture(passedChecklistTask)) } returns Checklist(id = 301, name = "Checklist", user = user)
        controller.removeRelation(user, checklistTask.checklistId, checklistTask.taskId)
        assertEquals(checklistTask, passedChecklistTask.captured)
        verify { checklistTaskService.removeTask(any()) }
    }

    @Test
    fun `should return 404 with description for task not found`() {
        val response = controller.taskNotFoundException()
        val expectedResponse = ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(statusCode = 404, name = "Not Found", description = "Task could not be found"))
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `should return 404 with description for checklist not found`() {
        val response = controller.checklistNotFoundException()
        val expectedResponse = ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(statusCode = 404, name = "Not Found", description = "Checklist could not be found"))
        assertEquals(expectedResponse, response)
    }
}