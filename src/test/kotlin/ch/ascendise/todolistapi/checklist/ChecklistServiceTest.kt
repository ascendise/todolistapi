package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskService
import ch.ascendise.todolistapi.user.User
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class ChecklistServiceTest {

    private val checklistRepository = mockk<ChecklistRepository>()
    private val taskService = mockk<TaskService>()
    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")
    private lateinit var service: ChecklistService

    @BeforeEach
    fun setUp() {
        service = ChecklistService(checklistRepository, taskService)
    }

    @Test
    fun `getChecklists() should return all checklists`() {
        //Arrange
        val expectedChecklists = listOf(Checklist(name = "New Checklist1", user = user), Checklist(name = "New Checklist2", user = user))
        every { checklistRepository.findAllByUserId(user.id) } returns expectedChecklists
        //Act
        val checklists = service.getChecklists(user.id)
        //Assert
        assertEquals(expectedChecklists, checklists)
        verify { checklistRepository.findAllByUserId(user.id) }
    }

    @Test
    fun `getChecklists() should return checklists with empty list`() {
        //Arrange
        every { checklistRepository.findAllByUserId(user.id) } returns emptyList()
        //Act
        val checklists = service.getChecklists(user.id)
        //Assert
        assertEquals(emptyList<Checklist>(), checklists)
        verify { checklistRepository.findAllByUserId(user.id) }
    }

    @Test
    fun `getChecklist() should return checklist with specified id`() {
        //Arrange
        val expected = Checklist(id = 101, name = "New Checklist1", user = user)
        every { checklistRepository.findByIdAndUserId(expected.id, user.id) } returns Optional.of(expected)
        //Act
        val checklist = service.getChecklist(expected.id, user.id)
        //Assert
        assertEquals(expected, checklist)
        verify { checklistRepository.findByIdAndUserId(expected.id, user.id) }
    }

    @Test
    fun `getChecklist() should throw ChecklistNotFoundException if no checklist with specified id exists`() {
        //Arrange
        val id = 101L
        every { checklistRepository.findByIdAndUserId(id, user.id) } returns Optional.empty()
        //Act
        assertThrows<ChecklistNotFoundException> { service.getChecklist(id, user.id) }
        //Assert
        verify { checklistRepository.findByIdAndUserId(id, user.id) }
    }

    @Test
    fun `create() should create new checklist`() {
        //Arrange
        val newChecklist = Checklist(name = "New Checklist2", user = user)
        every { checklistRepository.save(newChecklist) } returns newChecklist
        //Act
        val returnedChecklist = service.create(newChecklist)
        //Assert
        assertEquals(newChecklist, returnedChecklist)
        verify { checklistRepository.save(newChecklist) }
    }

    @Test
    fun `update() should update existing checklist`() {
        //Arrange
        val checklist = Checklist(id = 101, name = "Shopping List", user = user)
        val oldChecklist = Checklist(id = 101, name = "New Checklist", user = user)
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(oldChecklist)
        every { checklistRepository.save(oldChecklist) } returns checklist
        //Act
        val newChecklist = service.update(checklist)
        //Assert
        assertEquals(checklist, newChecklist)
        verify { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
        verify { checklistRepository.save(oldChecklist) }
    }

    @Test
    fun `update() should throw ChecklistNotFoundException if to be updated checklist does not exist`() {
        //Arrange
        val checklist = Checklist(id = -1L, name = "Shopping List", user = user)
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.empty()
        //Act
        assertThrows<ChecklistNotFoundException> { service.update(checklist) }
        //Assert
        verify { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
    }

    @Test
    fun `delete() should delete checklist`() {
        //Arrange
        val checklistId = 101L
        justRun { checklistRepository.deleteByIdAndUserId(checklistId, user.id) }
        //Act
        service.delete(checklistId, user.id)
        //Assert
        verify { checklistRepository.deleteByIdAndUserId(checklistId, user.id) }
    }

    @Test
    fun `complete() should delete checklist and tasks inside of it`() {
        //Arrange
        val task = Task(id = 201, name = "Task", user = user, isDone = true)
        val task2 = Task(id = 202, name = "Task 2", user = user, isDone = true)
        val checklist = Checklist(id = 301, name = "Checklist", user = user, tasks = mutableListOf(task, task2))
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(checklist)
        justRun { taskService.delete(user.id, any()) }
        justRun { checklistRepository.deleteByIdAndUserId(checklist.id, user.id) }
        //Act
        service.complete(checklist.id, user.id)
        //Assert
        verify(exactly = 1) { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
        verify(exactly = 1) { taskService.delete(user.id, 201) }
        verify(exactly = 1) { taskService.delete(user.id, 202) }
        verify(exactly = 1) { checklistRepository.deleteByIdAndUserId(checklist.id, user.id) }
    }

    @Test
    fun `complete() should throw exception if not all tasks are done`() {
        //Arrange
        val task = Task(id = 201, name = "Task", user = user, isDone = false)
        val task2 = Task(id = 202, name = "Task 2", user = user, isDone = true)
        val checklist = Checklist(id = 301, name = "Checklist", user = user, tasks = mutableListOf(task, task2))
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(checklist)
        justRun { taskService.delete(user.id, any()) }
        justRun { checklistRepository.deleteByIdAndUserId(checklist.id, user.id) }
        //Act
        val exception = assertThrows<ChecklistIncompleteException> { service.complete(checklist.id, user.id) }
        //Assert
        assertEquals("Checklist cannot be completed as it includes undone tasks", exception.message)
        verify(exactly = 1) { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
        verify(exactly = 0) { taskService.delete(user.id, 201) }
        verify(exactly = 0) { taskService.delete(user.id, 202) }
        verify(exactly = 0) { checklistRepository.deleteByIdAndUserId(checklist.id, user.id) }
    }
}


























