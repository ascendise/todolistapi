package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskService
import org.springframework.stereotype.Service

@Service
class ChecklistTaskService(
    val checklistService: ChecklistService,
    val taskService: TaskService
) {

    fun addTask(checklistId: Long, userId: Long, task: Task) : Checklist{
        val checklist = checklistService.getChecklist(checklistId, userId)
        val newTask = taskService.create(task)
        checklist.tasks.add(newTask)
        return checklistService.update(checklist)
    }
}