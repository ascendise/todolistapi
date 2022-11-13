package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.user.User
import io.mockk.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.ResponseEntity
import java.net.URI
import java.time.LocalDate

internal class TaskControllerTest {

    private lateinit var controller: TaskController
    private val taskService = mockk<TaskService>()
    private val taskModelAssembler = TaskModelAssembler()
    private val user = User(id = 101, username = "John Doe", subject = "auth|12345")

    @BeforeEach
    fun setUp() {
        controller = TaskController(taskService, taskModelAssembler)
    }

    @Test
    fun `should return tasks mapped to user`() {
        val tasks = setOf(
            Task(id = 201, name = "Task1", user = user),
            Task(id = 202, name = "Task2", user = user))
        every { taskService.getAll(user.id) } returns tasks
        val response = controller.getTasks(user)
        val expectedTaskDto = listOf(
            TaskResponseDto(id = 201, name = "Task1"),
            TaskResponseDto(id = 202, name = "Task2")
        )
        addExpectedTaskLinks(expectedTaskDto[0])
        addExpectedTaskLinks(expectedTaskDto[1])
        val expectedResponse = CollectionModel.of(expectedTaskDto,
            linkTo<TaskController> { getTasks(user) }.withSelfRel())
        assertEquals(expectedResponse, response)
        verifySequence {
            taskService.getAll(user.id)
        }
    }

    private fun addExpectedTaskLinks(dto: TaskResponseDto) {
        dto.add(linkTo<TaskController> { getTask(user, dto.id) }.withSelfRel())
        dto.add(linkTo<TaskController> { getTasks(user) }.withRel("tasks"))
    }

    @Test
    fun `should return specified task of user`() {
        val task = Task(id = 201, name = "Task", user = user)
        every { taskService.getById(user.id, task.id) } returns task
        val expectedResponse = TaskResponseDto(id = 201, name = "Task")
        addExpectedTaskLinks(expectedResponse)
        val response = controller.getTask(user, 201)
        assertEquals(expectedResponse, response)
        verify { taskService.getById(user.id, task.id) }
    }

    @Test
    fun `should throw TaskNotFoundException if no task with id for user exists`() {
        val taskId = 999L
        every { taskService.getById(user.id, taskId) } throws TaskNotFoundException()
        assertThrows<TaskNotFoundException> { controller.getTask(user, 999L) }
        verify { taskService.getById(user.id, taskId) }
    }

    @Test
    fun `should create new task`() {
        val slot = slot<Task>()
        every { taskService.create(capture(slot)) } returnsArgument 0
        val taskRequest = TaskRequestDto(name = "Task1", description = "My new task", endDate = null, isDone = false)
        val responseDto = controller.createTask(user, taskRequest)
        val taskResponseDto = TaskResponseDto(id = 0, name = "Task1", description = "My new task")
        addExpectedTaskLinks(taskResponseDto)
        val expectedResponse = ResponseEntity.created(URI.create("/0")).body(taskResponseDto)
        assertEquals(expectedResponse, responseDto)
        val expectedTask = Task(name = "Task1", description = "My new task", user = user)
        assertEquals(expectedTask, slot.captured)
        verify { taskService.create(any()) }
    }

    @Test
    fun `should update task with specified id`() {
        val slot = slot<Task>()
        every { taskService.update(capture(slot)) } returns Task(name = "Task1", description = "My new task", user = user)
        val taskRequest = TaskRequestDto(name = "Task1", description = "My new task", endDate = null, isDone = false)
        val response = controller.putTask(user, 201, taskRequest)
        val expectedTaskResponse = TaskResponseDto(name = "Task1", description = "My new task")
        addExpectedTaskLinks(expectedTaskResponse)
        val expectedResponse = ResponseEntity.ok().body(expectedTaskResponse)
        assertEquals(expectedResponse, response)
        val expectedTask = Task(id = 201, name = "Task1", description = "My new task", user = user)
        assertEquals(expectedTask, slot.captured)
        verify { taskService.update(any()) }
    }

    @Test
    fun `should throw TaskNotFoundException if task with id does not exist`() {
        val request = TaskRequestDto(name = "Task1", description = "My new task", endDate = null, isDone = false)
        val taskPassedToService = Task(id = 999, name = "Task1", description = "My new task", user = user)
        every { taskService.update(taskPassedToService) } throws TaskNotFoundException()
        assertThrows<TaskNotFoundException> { controller.putTask(user, 999L, request) }
        verify { taskService.update(taskPassedToService) }
    }

    @Test
    fun `should throw InvalidDateRangeTaskException if updated task has end date before start date`() {
        val taskPassedToService = Task(id = 201L, name = "Task1", description = "My new Task",
            startDate = LocalDate.now(), endDate = LocalDate.now().minusDays(1), user = user)
        every { taskService.update(taskPassedToService) } throws InvalidDateRangeTaskException()
        val taskId = 201L
        val taskRequest = TaskRequestDto(name = "Task1", description = "My new Task",  startDate = LocalDate.now(),
            endDate = LocalDate.now().minusDays(1), isDone = false)
        assertThrows<InvalidDateRangeTaskException> { controller.putTask(user, taskId, taskRequest) }
        verify { taskService.update(taskPassedToService) }
    }

    @Test
    fun `should delete task and return no content`() {
        val taskId = 201L
        justRun { taskService.delete(user.id, taskId) }
        val response = controller.deleteTask(user, taskId)
        val expectedResponse = ResponseEntity.noContent().build<TaskResponseDto>()
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `should return error message for InvalidTaskException`() {
        val response = controller.invalidTaskHandler(InvalidTaskException())
        val expectedResponse = ResponseEntity.unprocessableEntity().body(
            ApiError(
                statusCode = 422,
                name = "Unprocessable Entity",
                description = ""
            )
        )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `should return a "Not Found" response for TaskNotFoundException`() {
        val response = controller.taskNotFoundException(TaskNotFoundException())
        val expectedResponse = ResponseEntity.notFound().build<ApiError>()
        assertEquals(expectedResponse, response)
    }
}