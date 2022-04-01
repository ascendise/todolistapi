package ch.ascendise.todolistapi.task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TaskService(
    val taskRepository: TaskRepository
) {

    fun createTask(task: Task) {
        if(task.endDate?.isBefore(task.startDate) == true)
        {
            throw InvalidTaskException()
        }
        taskRepository.save(task)
    }

}