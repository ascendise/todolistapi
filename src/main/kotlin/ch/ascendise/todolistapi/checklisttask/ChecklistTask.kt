package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.user.User

data class ChecklistTask(
    val checklistId: Long,
    val taskId: Long,
    val userId: Long
) {

    constructor(checklist: Checklist, task: Task, user: User) :
            this(checklist.id, task.id, user.id)
}