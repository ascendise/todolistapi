package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.server.mvc.linkTo

internal class TaskModelAssemblerTest {

    private lateinit var taskModelAssembler: TaskModelAssembler

    @BeforeEach
    fun setUp() {
        taskModelAssembler = TaskModelAssembler()
    }

    @Test
    fun `should return task with links to specific operations`() {
        val user = User(id = 101, subject = "auth|54321")
        val task = Task(id = 201, name = "Task1", description = "My new task", user = user)
        val taskWithLinks = taskModelAssembler.toModel(task)
        val expectedTaskWithLinks = TaskResponseDto(id = 201, name = "Task1", description = "My new task").let {
            it.add(linkTo<TaskController> { getTask(user, 201) }.withSelfRel())
            it.add(linkTo<TaskController> { getTasks(user) }.withRel("tasks") )
        }
        assertEquals(expectedTaskWithLinks, taskWithLinks)
    }

    @Test
    fun `should return TaskResponseDto with links to specific operation`() {
        val user = User(id = 101, subject = "auth|54321")
        val taskResponse = TaskResponseDto(id = 201, name = "Task1", description = "My new task")
        val taskResponseWithLinks = taskModelAssembler.toModel(taskResponse, user)
        val expectedTaskResponseWihtLink = TaskResponseDto(id = 201, name = "Task1", description = "My new task").let {
            it.add(linkTo<TaskController> { getTask(user, 201) }.withSelfRel())
            it.add(linkTo<TaskController> { getTasks(user) }.withRel("tasks") )
        }
        assertEquals(expectedTaskResponseWihtLink, taskResponseWithLinks)
    }
}