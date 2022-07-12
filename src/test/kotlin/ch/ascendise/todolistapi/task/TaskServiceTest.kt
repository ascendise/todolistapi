package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.*

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private lateinit var taskService: TaskService

    @MockkBean
    private lateinit var taskRepository: TaskRepository

    @Test
    fun `Create new task`(){
        val task = Task(
            name = "Test",
            description = "Test TaskService",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Can't create task with start date that starts after end date`()
    {
        val task = Task(
            name = "Test",
            description = "Test TaskService",
            startDate = LocalDate.now().plusDays(1),
            endDate = LocalDate.now(),
            user = User(username = "", subject = "")
        )
        assertThrows<InvalidTaskException> { taskService.create(task) }
    }

    @Test
    fun `Create task with minimal information`()
    {
        val task = Task(
            name = "",
            description = "",
            endDate = null,
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Create task with only a start date`()
    {
        val task = Task(
            name = "",
            description = "",
            startDate = LocalDate.now().plusDays(1),
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Create task with only an end date`()
    {
        val task = Task(
            name = "",
            description = "",
            endDate = LocalDate.now(),
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Can't create task that starts before today`()
    {
        val task = Task(
            name = "",
            description = "",
            startDate = LocalDate.now().minusDays(1),
            user = User(username = "", subject = "")
        )
        assertThrows<InvalidTaskException> { taskService.create(task) }
    }

    @Test
    fun `Get tasks for user`()
    {
        val task1 = Task(name = "Task1", description = "Task1", startDate = LocalDate.now(),
            user = User(username = "", subject = ""))
        val task2 = Task(name = "Task2", description = "Task2", endDate = LocalDate.now(),
            user = User(username = "", subject = ""))
        every { taskRepository.findAllByUserId(1) } returns listOf(task1, task2)
        taskService.getAll(1)
        verify { taskRepository.findAllByUserId(1) }
    }

    @Test
    fun `Delete task`()
    {
        justRun { taskRepository.deleteByIdAndUserId(1, 1) }
        taskService.delete(1, 1)
        verify { taskRepository.deleteByIdAndUserId(1, 1)}
    }

    @Test
    fun `Return specific task`() {
        val user = User(id = 1, subject = "auth-oauth2|123451234512345", username = "Max")
        every { taskRepository.findByIdAndUserId(1, 1)} returns Optional.of(Task(name = "Dummy", user = user))
        taskService.getById(user.id, 1)
        verify { taskRepository.findByIdAndUserId(1, 1)}
    }

    @Test
    fun `Throw exception when task is not found`() {
        val user = User(id = 1, subject = "auth-oauth2|123451234512345", username = "Max")
        every { taskRepository.findByIdAndUserId(101, 1) } returns Optional.empty()
        assertThrows<TaskNotFoundException> { taskService.getById(user.id, 101) }
    }

    @Test
    fun `Update task`() {
        val user = User(id = 1, subject = "auth-oauth2|123451234512345", username = "Max")
        val task = Task(id = 1, name = "Updated Task", description = "This task has a new description", user = user)
        every { taskRepository.findByIdAndUserId(task.id, user.id) } returns Optional.of(Task(id = 1, name = "Old task", description = "This task is old", user = user))
        every {taskRepository.save(task)} returns task
        taskService.update(task)
        verify {taskRepository.save(task)}
        verify { taskRepository.findByIdAndUserId(task.id, user.id) }
    }

    @Test
    fun `Throw TaskNotFoundException if task was not found`() {
        val user = User(id = 1, subject = "auth-oauth2|123451234512345", username = "Max")
        val task = Task(id = 1, name = "Updated Task", description = "This task has a new description", user = user)
        every { taskRepository.findByIdAndUserId(task.id, user.id) } returns Optional.empty()
        assertThrows<TaskNotFoundException> { taskService.update(task) }
    }
}