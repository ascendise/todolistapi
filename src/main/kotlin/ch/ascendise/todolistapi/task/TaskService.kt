package ch.ascendise.todolistapi.task

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

    fun update(task: Task): Task {
        validate(task)
        val oldTask = taskRepository.findByIdAndUserId(task.id, task.user.id).orElseThrow { TaskNotFoundException()}
        oldTask.name = task.name
        oldTask.description = task.description
        oldTask.startDate = task.startDate
        oldTask.endDate = task.endDate
        oldTask.isDone = task.isDone
        return taskRepository.save(oldTask)
    }

    fun getAll(userId: Long): Set<Task> = taskRepository.findAllByUserId(userId).toSet()
    fun delete(userId: Long, taskId: Long) = taskRepository.deleteByIdAndUserId(taskId, userId)
    fun getById(userId: Long, taskId: Long): Task =
        taskRepository.findByIdAndUserId(taskId, userId).orElseThrow { TaskNotFoundException() }


}