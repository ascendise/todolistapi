package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.task.TaskModelAssembler
import ch.ascendise.todolistapi.task.TaskResponseDto
import ch.ascendise.todolistapi.user.User
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.ResponseEntity
import java.net.URI

internal class ChecklistControllerTest
{
    private lateinit var controller: ChecklistController

    private val checklistService = mockk<ChecklistService>()
    private val checklistModelAssembler = ChecklistModelAssembler(TaskModelAssembler())
    private val user = User(id = 101, username = "Max Muster", subject = "auth|54321")

    @BeforeEach
    fun setUp() {
        controller = ChecklistController(checklistService, checklistModelAssembler)
    }

    @Test
    fun `should return list of checklists associated with user`() {
        val checklists = listOf(
            Checklist(id = 301, name = "Checklist1", tasks = mutableListOf(
                Task(id = 201, name = "Task1", user = user),
                Task(id = 202, name = "Task2", user = user)
            ), user = user),
            Checklist(id = 302, name = "Checklist2", user = user)
        )
        every { checklistService.getChecklists(user.id) } returns checklists
        val response = controller.getChecklists(user)
        val expectedChecklists = listOf(
            ChecklistResponseDto(id = 301, name = "Checklist1", tasks = mutableListOf(
                TaskResponseDto(id = 201, name = "Task1"),
                TaskResponseDto(id = 202, name = "Task2")
            )),
            ChecklistResponseDto(id = 302, name = "Checklist2")
        )
        addExpectedLinks(expectedChecklists[0])
        addExpectedLinks(expectedChecklists[1])
        val expectedResponse = CollectionModel.of(expectedChecklists,
            linkTo<ChecklistController> { getChecklists(user) }.withSelfRel(),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations"),
        )
        assertEquals(expectedResponse, response)
        verify { checklistService.getChecklists(user.id) }
    }

    private fun addExpectedLinks(dto: ChecklistResponseDto) {
        dto.add(
            linkTo<ChecklistController> { getChecklist(dto.id, user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations"),
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
    fun `should return specified checklist`() {
        val checklistId = 301L
        val checklist = Checklist(id = 301, name = "Checklist1", user = user, tasks = mutableListOf(
            Task(id = 201, name = "Task1", user = user),
            Task(id = 202, name = "Task2", user = user)
        ))
        every { checklistService.getChecklist(checklistId, user.id) } returns checklist
        val response = controller.getChecklist(checklistId, user)
        val expectedResponse = ChecklistResponseDto(id = 301 , name = "Checklist1", tasks = mutableListOf(
            TaskResponseDto(id = 201, name = "Task1"),
            TaskResponseDto(id = 202, name = "Task2")
        )).apply { addExpectedLinks(this) }
        assertEquals(expectedResponse, response)
        verify { checklistService.getChecklist(checklistId, user.id) }
    }

    @Test
    fun `should throw ChecklistNotFoundException when getting nonexisting checklist`() {
        val checklistId = 999L
        every { checklistService.getChecklist(checklistId, user.id) } throws ChecklistNotFoundException()
        assertThrows<ChecklistNotFoundException> { controller.getChecklist(checklistId, user) }
        verify { checklistService.getChecklist(checklistId, user.id) }
    }

    @Test
    fun `should create Checklist and return the created entity`() {
        every { checklistService.create(Checklist(name = "Checklist1", user = user)) } returnsArgument 0
        val request = ChecklistRequestDto(name = "Checklist1")
        val response = controller.create(user, request)
        val expectedBody = ChecklistResponseDto(id = 0, name = "Checklist1").apply { addExpectedLinks(this) }
        val expectedResponse = ResponseEntity
            .created(URI.create("/checklists/0"))
            .body(expectedBody)
        assertEquals(expectedResponse, response)
        verify { checklistService.create(Checklist(name = "Checklist1", user = user)) }
    }

    @Test
    fun `should update checklist with given id and return updated checklist`() {
        every { checklistService.update(Checklist(id = 301, name = "Checklist2", user = user)) } returnsArgument 0
        val checklistId = 301L
        val request = ChecklistRequestDto(name = "Checklist2")
        val response = controller.update(checklistId, user, request)
        val expectedBody = ChecklistResponseDto(id = 301, name = "Checklist2").apply { addExpectedLinks(this) }
        val expectedResponse = ResponseEntity.ok(expectedBody)
        assertEquals(expectedResponse, response)
        verify { checklistService.update(Checklist(id = 301, name = "Checklist2", user = user)) }
    }

    @Test
    fun `should throw ChecklistNotFoundException when trying to update nonexisting checklist`() {
        val checklistId = 999L
        every {
            checklistService.update(Checklist(id = 999, name = "Checklist2", user = user))
        } throws ChecklistNotFoundException()
        val request = ChecklistRequestDto(name = "Checklist2")
        assertThrows<ChecklistNotFoundException> { controller.update(checklistId, user, request) }
        verify { checklistService.update(Checklist(id = 999, name = "Checklist2", user = user)) }
    }

    @Test
    fun `should delete specified checklist and return a No Content response`() {
        val checklistId = 301L
        justRun { checklistService.delete(checklistId, user.id) }
        val response = controller.delete(checklistId, user)
        val expectedResponse = ResponseEntity.noContent().build<Any>()
        assertEquals(expectedResponse, response)
        verify { checklistService.delete(checklistId, user.id) }
    }

    @Test
    fun `should return "Not Found"`() {
        val response = controller.checklistNotFoundException()
        val expectedResponse = ResponseEntity.notFound().build<Any>()
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `should complete checklist and return no content`() {
        val checklistId = 301L
        justRun { checklistService.complete(checklistId, user.id) }
        val response = controller.complete(checklistId, user);
        val expectedResponse = ResponseEntity.noContent().build<Any>()
        assertEquals(expectedResponse, response)
        verify { checklistService.complete(checklistId, user.id) }
    }
}
