package ch.ascendise.todolistapi.task

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TaskService(
    val taskRepository: TaskRepository
) {

    fun createTask(task: Task) {
        taskRepository.save(task)
    }

}