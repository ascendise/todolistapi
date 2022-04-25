package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskService
import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChecklistTaskServiceTest {

    @MockkBean
    private lateinit var checklistService: ChecklistService
    @MockkBean
    private lateinit var taskService: TaskService
    @Autowired
    private lateinit var checklistTaskService: ChecklistTaskService

    private val user = User(id = 1, username = "User", email = "mail@domain.com")

    @Test
    fun `Add new task to checklist`() {
        val task = Task(name = "New Task", user = user)
        val checklist = Checklist(id = 1, name = "New Checklist", user = user)
        every { taskService.create(task) } returns task
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        every {
            checklistService.update( match { it.id == 1L })
        } returns Checklist(id = checklist.id, name = checklist.name, tasks = mutableListOf(task), user = checklist.user)
        val updatedChecklist = checklistTaskService.addTask(checklist.id, user.id, task)
        verify { taskService.create(task) }
        verify { checklistService.getChecklist(checklist.id, user.id) }
        verify { checklistService.update(any()) }
        assertTrue(updatedChecklist.tasks.contains(task))
    }
}