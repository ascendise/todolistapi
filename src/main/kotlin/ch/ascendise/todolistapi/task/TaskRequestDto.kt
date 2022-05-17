package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import java.time.LocalDate

data class TaskRequestDto(
    val name: String,
    val description: String,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate?,
    val isDone: Boolean
    ) {

    fun toTask(user: User): Task = Task(
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            isDone = this.isDone,
            user = user
        )
}