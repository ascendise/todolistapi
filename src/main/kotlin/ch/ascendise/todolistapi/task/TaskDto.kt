package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import java.time.LocalDate

data class TaskDto(
    val name: String,
    val description: String,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate?
    ) {

    fun toTask(user: User): Task = Task(
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            user = user
        )
}