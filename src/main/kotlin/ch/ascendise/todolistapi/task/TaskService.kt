package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TaskService(
    val taskRepository: TaskRepository
) {

    fun put(task: Task) {
        if (task.endDate?.isBefore(task.startDate) == true ||
            task.startDate.isBefore(LocalDate.now())
        ) {
            throw InvalidTaskException()
        }
        taskRepository.save(task)
    }

    fun getAll(userId: Long): Set<Task> = taskRepository.findAllByUserId(userId).toSet()
    fun delete(taskId: Long) = taskRepository.deleteById(taskId)
    fun getById(user: User, taskId: Long): Task {
        return taskRepository.findByUserIdAndTaskId(user.id, taskId)
    }


}