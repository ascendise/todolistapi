package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.checklisttask.ChecklistTaskService
import ch.ascendise.todolistapi.user.User
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.*

internal class TaskServiceTest {

    private lateinit var service: TaskService

    private val checklistTaskService = mockk<ChecklistTaskService>()
    private val taskRepository = mockk<TaskRepository>()

    @BeforeEach
    fun setUp() {
        service = TaskService(taskRepository, checklistTaskService)
    }

    @Test
    fun `should create new task in database`(){
        val task = Task(
            name = "Test",
            description = "Test TaskService",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1),
            user = User(subject = "")
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
            user = User(subject = "")
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
            user = User(subject = "")
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
            user = User(subject = "")
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
            user = User(subject = "")
        )
        assertThrows<InvalidTaskException> { service.create(task) }
    }

    @Test
    fun `should return tasks for given user`()
    {
        val user = User(id = 101, subject = "auth|12345")
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
        justRun { checklistTaskService.removeTaskFromAllChecklists(taskId, userId) }
        service.delete(userId, taskId)
        verify { taskRepository.deleteByIdAndUserId(taskId, userId)}
        verify { checklistTaskService.removeTaskFromAllChecklists(taskId, userId) }
    }

    @Test
    fun `should return task with given user id`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
        val task = Task(id = 201, name = "Dummy", user = user)
        every { taskRepository.findByIdAndUserId(task.id, user.id)} returns Optional.of(task)
        val returnedTask = service.getById(user.id, task.id)
        assertEquals(task, returnedTask)
        verify { taskRepository.findByIdAndUserId(task.id, user.id)}
    }

    @Test
    fun `should throw exception when task is not found`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
        every { taskRepository.findByIdAndUserId(201, user.id) } returns Optional.empty()
        assertThrows<TaskNotFoundException> { service.getById(user.id, 201) }
    }

    @Test
    fun `should allow task with start date in past when updating`() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
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
        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
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
        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
        val task = Task(id = 201, name = "Updated Task", description = "This task has a new description", user = user)
        every { taskRepository.findByIdAndUserId(task.id, user.id) } returns Optional.empty()
        assertThrows<TaskNotFoundException> { service.update(task) }
        verify { taskRepository.findByIdAndUserId(task.id, user.id) }
    }

    @Test
    fun `should throw InvalidDateRangeTaskException if updated task has start date before end date `() {
        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
        val oldTask = Task(id = 201, name = "Old Task", user = user)
        val task = Task(id = 201, name = "Updated Task", user = user, endDate = LocalDate.now().minusDays(1))
        every { taskRepository.findByIdAndUserId(task.id, user.id) } returns Optional.of(oldTask)
        assertThrows<InvalidDateRangeTaskException> { service.update(task) }
        verify { taskRepository.findByIdAndUserId(task.id, user.id) }
    }

    @Test
    fun `should throw InvalidDateRangeTaskException if new start date is before old start date`() {

        val user = User(id = 101, subject = "auth-oauth2|123451234512345")
        val oldTask = Task(id = 201, name = "Old Task", user = user, startDate = LocalDate.now().plusDays(2))
        val task = Task(id = 201, name = "Updated Task", user = user, endDate = LocalDate.now().plusDays(1))
        every { taskRepository.findByIdAndUserId(task.id, user.id) } returns Optional.of(oldTask)
        assertThrows<InvalidDateRangeTaskException> { service.update(task) }
        verify { taskRepository.findByIdAndUserId(task.id, user.id) }
    }
}