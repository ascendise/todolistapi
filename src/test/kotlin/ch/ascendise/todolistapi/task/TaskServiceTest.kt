package ch.ascendise.todolistapi.task

import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
            description = "Test the createTask method",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(1)
        )
        every { taskRepository.save(task) } returns task
        taskService.createTask(task)
        verify { taskRepository.save(task) }
    }
}