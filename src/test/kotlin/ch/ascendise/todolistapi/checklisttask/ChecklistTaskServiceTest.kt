package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistRepository
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskRepository
import ch.ascendise.todolistapi.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class ChecklistTaskServiceTest {

    private lateinit var service: ChecklistTaskService

    private val taskRepo = mockk<TaskRepository>()
    private val checklistRepo =  mockk<ChecklistRepository>()

    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")

    @BeforeEach
    fun setUp() {
        service = ChecklistTaskService(taskRepo, checklistRepo)
    }

    @Test
    fun `addTask should add task to checklist`() {
        //Arrange
        val checklist = Checklist(id = 200, name = "Checklist1", user = user)
        val task = Task(id = 300, name = "Task", user = user)
        every { taskRepo.findByIdAndUserId(task.id, user.id) } returns Optional.of(task)
        every { checklistRepo.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(checklist)
        every { checklistRepo.save(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, task.id, user.id)
        //Act
        val updatedChecklist = service.addTask(checklistTask)
        //Assert
        assertTrue(updatedChecklist.tasks.contains(task))
        verify { taskRepo.findByIdAndUserId(task.id, user.id) }
        verify { checklistRepo.findByIdAndUserId(checklist.id, user.id) }
        verify { checklistRepo.save(any()) }
    }

    @Test
    fun `addTask should not add task to checklist, if it already is in there`() {
        //Arrange
        val task = Task(id = 300, name = "Task", user = user)
        val checklist = Checklist(id = 200, name = "Checklist1", user = user, tasks = mutableListOf(task))
        every { taskRepo.findByIdAndUserId(task.id, user.id) } returns Optional.of(task)
        every { checklistRepo.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(checklist)
        every { checklistRepo.save(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, task.id, user.id)
        //Act
        val updatedChecklist = service.addTask(checklistTask)
        //Assert
        assertTrue(updatedChecklist.tasks.size == 1)
        verify { taskRepo.findByIdAndUserId(task.id, user.id) }
        verify { checklistRepo.findByIdAndUserId(checklist.id, user.id) }
        verify(exactly = 0) { checklistRepo.save(any()) }
    }

    @Test
    fun `removeTask should remove task from checklist`() {
        //Arrange
        val task = Task(id = 300, name = "Task", user = user)
        val task2 = Task(id = 301, name = "Task2", user = user)
        val checklist = Checklist(id = 200, name = "Checklist1", user = user, tasks = mutableListOf(task, task2))
        every { checklistRepo.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(checklist)
        every { checklistRepo.save(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, task.id, user.id)
        //Act
        val updatedChecklist = service.removeTask(checklistTask)
        //Assert
        assertFalse(updatedChecklist.tasks.contains(task))
        assertTrue(updatedChecklist.tasks.contains(task2))
        verify { checklistRepo.findByIdAndUserId(checklist.id, user.id) }
        verify { checklistRepo.save(any()) }
    }

    @Test
    fun `removeTask should ignore remove if task doesn't exist in checklist`() {
        //Arrange
        val task = Task(id = 300, name = "Task", user = user)
        val task2 = Task(id = 301, name = "Task2", user = user)
        val checklist = Checklist(id = 200, name = "Checklist1", user = user, tasks = mutableListOf(task, task2))
        every { checklistRepo.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(checklist)
        every { checklistRepo.save(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, 100, user.id)
        //Act
        val updatedChecklist = service.removeTask(checklistTask)
        //Assert
        verify { checklistRepo.findByIdAndUserId(checklist.id, user.id) }
        verify { checklistRepo.save(any()) }
        assertEquals(checklist, updatedChecklist)
    }

    @Test
    fun `getRelations should return all known relations`() {
        //Arrange
        val task = Task(id = 300, name = "Task", user = user)
        val task2 = Task(id = 301, name = "Task2", user = user)
        val task3 = Task(id = 302, name = "Task3", user = user)
        val checklist1 = Checklist(id = 201, name = "Checklist1", user = user, tasks = mutableListOf(task))
        val checklist2 = Checklist(id = 202, name = "Checklist2", user = user, tasks = mutableListOf(task2, task3))
        val expectedChecklistTasks = mutableListOf(
            ChecklistTask(checklist1.id, task.id, user.id),
            ChecklistTask(checklist2.id, task2.id, user.id),
            ChecklistTask(checklist2.id, task3.id, user.id)
        )
        every { checklistRepo.findAllByUserId(user.id) } returns listOf(checklist1, checklist2)
        //Act
        val checklistTasks = service.getRelations(user.id)
        //Assert
        verify { checklistRepo.findAllByUserId(user.id) }
        assertEquals(expectedChecklistTasks, checklistTasks)
    }

    @Test
    fun `removeTaskFromAllChecklists should remove all relations betwenn a task and it's checklists`()
    {
        //Arrange
        val task = Task(id = 300, name = "Task", user = user);
        val checklist1 = Checklist(id = 201, name = "Checklist1", user = user, tasks = mutableListOf(task))
        val checklist2 = Checklist(id = 202, name = "Checklist2", user = user, tasks = mutableListOf(task))
        every { checklistRepo.findAllByUserId(user.id) } returns listOf(checklist1, checklist2)
        every { checklistRepo.saveAll(mutableListOf(checklist1, checklist2)) } returnsArgument 0
        //Act
        service.removeTaskFromAllChecklists(task.id, user.id)
        //Assert
        assertFalse(checklist1.tasks.contains(task))
        assertFalse(checklist2.tasks.contains(task))
        verify { checklistRepo.findAllByUserId(user.id) }
        verify  { checklistRepo.saveAll(mutableListOf(checklist1, checklist2)) }
    }
}