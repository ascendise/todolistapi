package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.time.LocalDate

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
            user = User(username = "", email = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.put(task)
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
            user = User(username = "", email = "")
        )
        assertThrows<InvalidTaskException> { taskService.put(task) }
    }

    @Test
    fun `Create task with minimal information`()
    {
        val task = Task(
            name = "",
            description = "",
            endDate = null,
            user = User(username = "", email = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.put(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Create task with only a start date`()
    {
        val task = Task(
            name = "",
            description = "",
            startDate = LocalDate.now().plusDays(1),
            user = User(username = "", email = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.put(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Create task with only an end date`()
    {
        val task = Task(
            name = "",
            description = "",
            endDate = LocalDate.now(),
            user = User(username = "", email = "")
        )
        every { taskRepository.save(task) } returns task
        taskService.put(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `Can't create task that starts before today`()
    {
        val task = Task(
            name = "",
            description = "",
            startDate = LocalDate.now().minusDays(1),
            user = User(username = "", email = "")
        )
        assertThrows<InvalidTaskException> { taskService.put(task) }
    }

    @Test
    fun `Get tasks for user`()
    {
        val task1 = Task(name = "Task1", description = "Task1", startDate = LocalDate.now(),
            user = User(username = "", email = ""))
        val task2 = Task(name = "Task2", description = "Task2", endDate = LocalDate.now(),
            user = User(username = "", email = ""))
        every { taskRepository.findAllByUserId(1) } returns listOf(task1, task2)
        taskService.getAll(1)
        verify { taskRepository.findAllByUserId(1) }
    }

    @Test
    fun `Delete task`()
    {
        justRun { taskRepository.deleteById(1) }
        taskService.delete(1)
        verify { taskRepository.deleteById(1)}
    }

    @Test
    fun `Return specific task`() {
        val user = User(id = 1, email = "mail@domain.com", username = "Max")
        every { taskRepository.findByUserIdAndTaskId(1, 1)} returns Task(name = "Dummy", user = user)
        taskService.getById(user, 1)
        verify { taskRepository.findByUserIdAndTaskId(1, 1)}
    }

    @Test
    fun `Throw exception when task is not found`() {
        val user = User(id = 1, email = "mail@domain.com", username = "Max")
        every { taskRepository.findByUserIdAndTaskId(1, 101) } returns null
        assertThrows<NotFoundException> { taskService.getById(user, 101) }
    }
}