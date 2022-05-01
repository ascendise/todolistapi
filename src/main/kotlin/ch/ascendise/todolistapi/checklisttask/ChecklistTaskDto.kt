package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "relations")
data class ChecklistTaskDto(
    val checklistId: Long,
    val taskId: Long
): RepresentationModel<ChecklistTaskDto>() {

    fun toChecklistTask(user: User) = ChecklistTask(checklistId, taskId, user.id)
}