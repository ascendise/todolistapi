package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistNotFoundException
import ch.ascendise.todolistapi.checklist.ChecklistRepository
import ch.ascendise.todolistapi.task.TaskNotFoundException
import ch.ascendise.todolistapi.task.TaskRepository
import org.springframework.stereotype.Service

@Service
class ChecklistTaskService(
    val taskRepository: TaskRepository,
    val checklistRepository: ChecklistRepository
){

    fun addTask(checklistTask: ChecklistTask): Checklist{
        val task = taskRepository.findByIdAndUserId(checklistTask.taskId, checklistTask.userId)
            .orElseThrow { TaskNotFoundException() }
        val checklist = checklistRepository.findByIdAndUserId(checklistTask.checklistId, checklistTask.userId)
            .orElseThrow { ChecklistNotFoundException() }
        if(checklist.tasks.contains(task))
        {
            return checklist
        }
        checklist.tasks.add(task)
        return checklistRepository.save(checklist)
    }

    fun removeTask(checklistTask: ChecklistTask): Checklist {
        val checklist = checklistRepository.findByIdAndUserId(checklistTask.checklistId, checklistTask.userId)
            .orElseThrow { ChecklistNotFoundException() }
        checklist.tasks.removeIf { it.id == checklistTask.taskId }
        return checklistRepository.save(checklist)
    }

    fun removeTaskFromAllChecklists(taskId: Long, userId: Long) {
        val checklists = checklistRepository.findAllByUserId(userId)
        checklists.forEach { it -> it.tasks.removeIf { t -> t.id == taskId } }
        checklistRepository.saveAll(checklists)
    }

    fun getRelations(userId: Long): List<ChecklistTask> {
        val checklists = checklistRepository.findAllByUserId(userId)
        val relations = mutableListOf<ChecklistTask>()
        for(checklist in checklists) {
            for(task in checklist.tasks) {
                relations.add(ChecklistTask(checklist.id, task.id, userId))
            }
        }
        return relations
    }
}