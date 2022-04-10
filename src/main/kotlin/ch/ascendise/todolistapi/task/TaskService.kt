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

    fun update(task: Task, taskId: Long, userId: Long): Task {
        validate(task)
        val oldTask = taskRepository.findByIdAndUserId(taskId, userId).orElseThrow { TaskNotFoundException()}
        oldTask.name = task.name
        oldTask.description = task.description
        oldTask.startDate = task.startDate
        oldTask.endDate = task.endDate
        return taskRepository.save(oldTask)
    }

    fun getAll(userId: Long): Set<Task> = taskRepository.findAllByUserId(userId).toSet()
    fun delete(userId: Long, taskId: Long) = taskRepository.deleteByIdAndUserId(taskId, userId)
    fun getById(user: User, taskId: Long): Task =
        taskRepository.findByIdAndUserId(taskId, user.id).orElseThrow { TaskNotFoundException() }


}