package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.checklist.ChecklistService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TaskService(
    val taskRepository: TaskRepository,
    val checklistService: ChecklistService,
) {

    fun create(task: Task): Task {
        validateNewTask(task)
        return taskRepository.save(task)
    }

    fun validateNewTask(task: Task) {
        if (task.endDate?.isBefore(task.startDate) == true) {
            throw InvalidDateRangeTaskException()
        } else if(task.startDate.isBefore(LocalDate.now())) {
            throw StartDateBeforeTodayTaskException()
        }
    }

    fun update(task: Task): Task {
        val oldTask = taskRepository.findByIdAndUserId(task.id, task.user.id).orElseThrow { TaskNotFoundException() }
        if (task.endDate?.isBefore(task.startDate) == true) {
            throw InvalidDateRangeTaskException()
        }else if (task.startDate.isBefore(oldTask.startDate)) {
            throw InvalidDateRangeTaskException()
        }
        oldTask.name = task.name
        oldTask.description = task.description
        oldTask.startDate = task.startDate
        oldTask.endDate = task.endDate
        oldTask.isDone = task.isDone
        return taskRepository.save(oldTask)
    }

    fun getAll(userId: Long): Set<Task> = taskRepository.findAllByUserId(userId).toSet()

    fun delete(userId: Long, taskId: Long) {
        removeTaskFromChecklists(userId, taskId)
        taskRepository.deleteByIdAndUserId(taskId, userId)
    }

    private fun removeTaskFromChecklists(userId: Long, taskId: Long) {
        val checklists = checklistService.getChecklists(userId)
        checklists.filter { it.tasks.any { t -> t.id == taskId } }
            .forEach {
                it.tasks.removeIf { t -> t.id == taskId }
                checklistService.update(it)
            }
    }

    fun getById(userId: Long, taskId: Long): Task =
        taskRepository.findByIdAndUserId(taskId, userId).orElseThrow { TaskNotFoundException() }


}