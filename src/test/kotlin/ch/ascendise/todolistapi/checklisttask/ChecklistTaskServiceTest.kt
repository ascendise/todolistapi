package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskService
import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChecklistTaskServiceTest {

    @Autowired
    private lateinit var service: ChecklistTaskService

    @MockkBean
    private lateinit var taskService: TaskService

    @MockkBean
    private lateinit var checklistService: ChecklistService

    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")

    @Test
    fun `Add task to checklist`() {
        val checklist = Checklist(id = 200, name = "Checklist1", user = user)
        val task = Task(id = 300, name = "Task", user = user)
        every { taskService.getById(user.id, task.id) } returns task
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every { checklistService.update(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, task.id, user.id)
        val updatedChecklist = service.addTask(checklistTask)
        verify { taskService.getById(user.id, task.id) }
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify { checklistService.update(any()) }
        assertTrue(updatedChecklist.tasks.contains(task))
    }

    @Test
    fun `Don't add task to checklist, if it already is in there`() {
        val task = Task(id = 300, name = "Task", user = user)
        val checklist = Checklist(id = 200, name = "Checklist1", user = user, tasks = mutableListOf(task))
        every { taskService.getById(user.id, task.id) } returns task
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every { checklistService.update(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, task.id, user.id)
        val updatedChecklist = service.addTask(checklistTask)
        verify { taskService.getById(user.id, task.id) }
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify(exactly = 0) { checklistService.update(any()) }
        assertTrue(updatedChecklist.tasks.size == 1)
    }

    @Test
    fun `Remove task from checklist`() {
        val task = Task(id = 300, name = "Task", user = user)
        val task2 = Task(id = 301, name = "Task2", user = user)
        val checklist = Checklist(id = 200, name = "Checklist1", user = user, tasks = mutableListOf(task, task2))
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every { checklistService.update(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, task.id, user.id)
        val updatedChecklist = service.removeTask(checklistTask)
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify { checklistService.update(any()) }
        assertFalse(updatedChecklist.tasks.contains(task))
        assertTrue(updatedChecklist.tasks.contains(task2))
    }

    @Test
    fun `Ignore remove if task doesn't exist in checklist`() {
        val task = Task(id = 300, name = "Task", user = user)
        val task2 = Task(id = 301, name = "Task2", user = user)
        val checklist = Checklist(id = 200, name = "Checklist1", user = user, tasks = mutableListOf(task, task2))
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every { checklistService.update(any()) } returnsArgument 0
        val checklistTask = ChecklistTask(checklist.id, 100, user.id)
        val updatedChecklist = service.removeTask(checklistTask)
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify { checklistService.update(any()) }
        assertEquals(checklist, updatedChecklist)
    }

    @Test
    fun `Return all known relations`() {
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
        every { checklistService.getChecklists(user.id) } returns listOf(checklist1, checklist2)
        val checklistTasks = service.getRelations(user.id)
        verify { checklistService.getChecklists(user.id) }
        assertEquals(expectedChecklistTasks, checklistTasks)
    }
}