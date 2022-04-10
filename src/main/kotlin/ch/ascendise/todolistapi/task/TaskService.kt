package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TaskService(
    val taskRepository: TaskRepository
) {

    fun create(task: Task): Task {
        validate(task)
        return taskRepository.save(task)
    }

    fun validate(task: Task) {
        if (task.endDate?.isBefore(task.startDate) == true) {
            throw InvalidDateRangeTaskException()
        } else if(task.startDate.isBefore(LocalDate.now())) {
            throw StartDateBeforeTodayTaskException()
        }
    }

    fun update(task: Task, id: Long): Task {
        validate(task)
        val oldTask = taskRepository.findById(id).get()
        oldTask.name = task.name
        oldTask.description = task.description
        oldTask.startDate = task.startDate
        oldTask.endDate = task.endDate
        return taskRepository.save(oldTask)
    }

    fun getAll(userId: Long): Set<Task> = taskRepository.findAllByUserId(userId).toSet()
    fun delete(taskId: Long) = taskRepository.deleteById(taskId)
    fun getById(user: User, taskId: Long): Task =
        taskRepository.findByIdAndUserId(taskId, user.id) ?: throw NotFoundException()


}