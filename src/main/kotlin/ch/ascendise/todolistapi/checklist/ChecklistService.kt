package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.task.TaskService
import org.springframework.stereotype.Service

@Service
class ChecklistService(
    val checklistRepository: ChecklistRepository,
    val taskService: TaskService
) {

    fun getChecklists(userId: Long) : List<Checklist> =
        checklistRepository.findAllByUserId(userId)

    fun getChecklist(id: Long, userId: Long) : Checklist =
        checklistRepository.findByIdAndUserId(id, userId).orElseThrow { ChecklistNotFoundException() }

    fun create(checklist: Checklist) : Checklist =
        checklistRepository.save(checklist)

    fun update(checklist: Checklist) : Checklist {
        val oldChecklist = checklistRepository.findByIdAndUserId(checklist.id, checklist.user.id).orElseThrow {ChecklistNotFoundException()}
        oldChecklist.name = checklist.name
        oldChecklist.tasks = checklist.tasks
        return checklistRepository.save(oldChecklist)
    }

    fun delete(checklistId: Long, userId: Long) =
        checklistRepository.deleteByIdAndUserId(checklistId, userId)

    fun complete(checklistId: Long, userId: Long) {
        val checklist = checklistRepository.findByIdAndUserId(checklistId, userId)
            .orElseThrow { ChecklistNotFoundException() }
        if (checklist.tasks.any { !it.isDone }) {
            throw ChecklistIncompleteException();
        }
        checklist.tasks.map { it.id }.forEach { taskService.delete(userId, it) }
        delete(checklistId, userId)
    }
}