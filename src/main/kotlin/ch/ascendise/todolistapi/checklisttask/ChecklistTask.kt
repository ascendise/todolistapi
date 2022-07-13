package ch.ascendise.todolistapi.checklisttask

data class ChecklistTask(
    val checklistId: Long,
    val taskId: Long,
    val userId: Long
)