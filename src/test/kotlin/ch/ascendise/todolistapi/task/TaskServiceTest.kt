package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistRepository
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.*

class TaskServiceTest {

    private lateinit var service: TaskService

    private val checklistService = mockk<ChecklistService>()
    private val taskRepository = mockk<TaskRepository>()

    @BeforeEach
    fun setUp() {
        service = TaskService(taskRepository, checklistService)
    }

    @Test
    fun `should create new task in database`(){
        val task = Task(
            name = "Test",
            description = "Test TaskService",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        service.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `should throw InvalidTaskException if start date is after end date`()
    {
        val task = Task(
            name = "Test",
            description = "Test TaskService",
            startDate = LocalDate.now().plusDays(1),
            endDate = LocalDate.now(),
            user = User(username = "", subject = "")
        )
        assertThrows<InvalidDateRangeTaskException> { service.create(task) }
    }

    @Test
    fun `should allow to create task with minimal amount of information`()
    {
        val task = Task(
            name = "",
            description = "",
            endDate = null,
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        service.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `should allow to create task with start date only`()
    {
        val task = Task(
            name = "",
            description = "",
            startDate = LocalDate.now().plusDays(1),
            user = User(username = "", subject = "")
        )
        every { taskRepository.save(task) } returns task
        service.create(task)
        verify { taskRepository.save(task) }
    }

    @Test
    fun `should throw exception if task start date is before today`()
    {
        val task = Task(
            name = "",
            description = "",
            startDate = LocalDate.now().minusDays(1),
            user = User(username = "", subject = "")
        )
        assertThrows<InvalidTaskException> { service.create(task) }
    }

    @Test
    fun `should return tasks for given user`()
    {
        val user = User(id = 101, username = "John Doe", subject = "auth|12345")
        val task1 = Task(id = 201, name = "Task1", description = "Task1", startDate = LocalDate.now(), user = user)
        val task2 = Task(id = 202, name = "Task2", description = "Task2", endDate = LocalDate.now(), user = user)
        every { taskRepository.findAllByUserId(101) } returns listOf(task1, task2)
        service.getAll(101)
        verify { taskRepository.findAllByUserId(101) }
    }

    @Test
    fun `should delete task from repository`()
    {
        val userId = 101L
        val taskId = 201L
        justRun { taskRepository.deleteByIdAndUserId(taskId, userId) }
        every { checklistService.getChecklists(userId) } returns emptyList()
        service.delete(userId, taskId)
        verify { taskRepository.deleteByIdAndUserId(taskId, userId)}
        verify { checklistService.getChecklists(userId) }
    }

    @Test
    fun `should remove task from checklists and delete task`() {
        val user = User(id = 101, username = "", subject = "")
        val task = Task(id = 201, name = "Task", description = "Task1", startDate = LocalDate.now(), user = user)
        val checklist1 = Checklist(id = 301, name = "Checklist1", tasks = mutableListOf(task), user = user)
        val checklist2 = Checklist(id = 302, name = "Checklist2", tasks = mutableListOf(task), user = user)
        val checklist3 = Checklist(id = 303, name = "Checklist3", tasks = mutableListOf(), user = user)
        every { checklistService.getChecklists(user.id) } returns listOf(checklist1, checklist2, checklist3)
        every { checklistService.update(any()) } returnsArgument 0
        justRun { taskRepository.deleteByIdAndUserId(task.id, user.id) }
        service.delete(user.id, task.id)
        verify { checklistService.getChecklists(user.id) }
        verify {
            checklistService.update(withArg {
                assertFalse(it.tasks.contains(task))
            })
        }
        verify { taskRepository.deleteByIdAndUserId(task.id, user.id)}
    }

    @Test
    fun `should return task with given user id`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345", username = "Max")
        val task = Task(id = 201, name = "Dummy", user = user)
        every { taskRepository.findByIdAndUserId(task.id, user.id)} returns Optional.of(task)
        val returnedTask = service.getById(user.id, task.id)
        assertEquals(task, returnedTask)
        verify { taskRepository.findByIdAndUserId(task.id, user.id)}
    }

    @Test
    fun `should throw exception when task is not found`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345", username = "Max")
        every { taskRepository.findByIdAndUserId(201, user.id) } returns Optional.empty()
        assertThrows<TaskNotFoundException> { service.getById(user.id, 201) }
    }

    @Test
    fun `should allow task with start date in past when updating`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345", username = "Max")
        val oldTask = Task(id = 201, name = "Old Task", description = "This task has an old description",
            startDate = LocalDate.now().minusDays(1), isDone = false, user = user)
        val newTask = Task(id = 201, name = "Updated Task", description = "This task has a new description",
            startDate = LocalDate.now().minusDays(1), isDone = true, user = user)
        every { taskRepository.findByIdAndUserId(oldTask.id, user.id) } returns Optional.of(oldTask)
        every {taskRepository.save(any()) } returnsArgument 0
        assertDoesNotThrow { service.update(newTask) }
        verify { taskRepository.findByIdAndUserId(oldTask.id, user.id) }
        verify {taskRepository.save(any<Task>()) }
    }

    @Test
    fun `should update task`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345", username = "Max")
        val oldTask = Task(id = 201, name = "Old Task", description = "This task has an old description", isDone = false, user = user)
        val newTask = Task(id = 201, name = "Updated Task", description = "This task has a new description", isDone = true, user = user)
        every { taskRepository.findByIdAndUserId(oldTask.id, user.id) } returns Optional.of(oldTask)
        every {taskRepository.save(any()) } returnsArgument 0
        val updatedTask = service.update(newTask)
        verify { taskRepository.findByIdAndUserId(oldTask.id, user.id) }
        verify {taskRepository.save(any<Task>()) }
        assertEquals(updatedTask, newTask)
    }

    @Test
    fun `should throw TaskNotFoundException when task to be updated was not found`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345", username = "Max")
        val task = Task(id = 201, name = "Updated Task", description = "This task has a new description", user = user)
        every { taskRepository.findByIdAndUserId(task.id, user.id) } returns Optional.empty()
        assertThrows<TaskNotFoundException> { service.update(task) }
    }
}