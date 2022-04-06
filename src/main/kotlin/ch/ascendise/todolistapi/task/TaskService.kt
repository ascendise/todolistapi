package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TaskService(
    val taskRepository: TaskRepository
) {

    fun put(task: Task): Task {
        if (task.endDate?.isBefore(task.startDate) == true) {
            throw InvalidDateRangeTaskException()
        } else if(task.startDate.isBefore(LocalDate.now())) {
            throw StartDateBeforeTodayTaskException()
        }
        return taskRepository.save(task)
    }

    fun getAll(userId: Long): Set<Task> = taskRepository.findAllByUserId(userId).toSet()
    fun delete(taskId: Long) = taskRepository.deleteById(taskId)
    fun getById(user: User, taskId: Long): Task =
        taskRepository.findByIdAndUserId(taskId, user.id) ?: throw NotFoundException()


}