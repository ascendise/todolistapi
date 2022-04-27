package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.user.User

data class ChecklistTaskDto(
    val checklistId: Long,
    val taskId: Long
) {

    fun toChecklistTask(user: User) = ChecklistTask(checklistId, taskId, user.id)
}