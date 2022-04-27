package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.TaskService
import org.springframework.stereotype.Service

@Service
class ChecklistTaskService(
    val taskService: TaskService,
    val checklistService: ChecklistService
){

    fun addTask(checklistId: Long, taskId: Long, userId: Long): Checklist{
        val task = taskService.getById(userId, taskId)
        val checklist = checklistService.getChecklist(checklistId, userId)
        checklist.tasks.add(task)
        return checklistService.update(checklist)
    }

    fun removeTask(checklistId: Long, taskId: Long, userId: Long): Checklist{
        val checklist = checklistService.getChecklist(checklistId, userId)
        checklist.tasks.removeIf { it.id == taskId }
        return checklistService.update(checklist)
    }
}