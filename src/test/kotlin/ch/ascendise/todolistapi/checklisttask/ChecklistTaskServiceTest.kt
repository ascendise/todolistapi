package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistRepository
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskRepository
import ch.ascendise.todolistapi.task.TaskService
import ch.ascendise.todolistapi.user.User
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse

@SpringBootTest
class ChecklistTaskServiceTest {

    @Autowired
    private lateinit var service: ChecklistTaskService

    @MockkBean
    private lateinit var taskService: TaskService

    @MockkBean
    private lateinit var checklistService: ChecklistService

    private val user = User(id = 1, username = "User", email = "mail@domain.com")

    @Test
    fun `Add task to checklist`() {
        val checklist = Checklist(id = 1, name = "Checklist1", user = user)
        val task = Task(id = 1, name = "Task", user = user)
        every { taskService.getById(user.id, task.id) } returns task
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every { checklistService.update(any()) } returnsArgument 0
        val updatedChecklist = service.addTask(checklist.id, task.id, user.id)
        verify { taskService.getById(user.id, task.id) }
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify { checklistService.update(any()) }
        assertTrue(updatedChecklist.tasks.contains(task))
    }

    @Test
    fun `Remove task from checklist`() {
        val task = Task(id = 1, name = "Task", user = user)
        val task2 = Task(id = 2, name = "Task2", user = user)
        val checklist = Checklist(id = 1, name = "Checklist1", user = user, tasks = mutableListOf(task, task2))
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every { checklistService.update(any()) } returnsArgument 0
        val updatedChecklist = service.removeTask(checklist.id, task.id, user.id)
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify { checklistService.update(any()) }
        assertFalse(updatedChecklist.tasks.contains(task))
        assertTrue(updatedChecklist.tasks.contains(task2))
    }

}