package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

@Relation(collectionRelation = "tasks", itemRelation = "task")
open class TaskResponseDto(
    var id: Long = 0,
    var name: String,
    var description: String = "",
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null,
    var isDone: Boolean = false,
): RepresentationModel<TaskResponseDto>() {

    fun toTask(user: User) =
        Task(
            id = this.id,
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            isDone = this.isDone,
            user = user
        )

}

fun Task.toTaskResponseDto() =
    TaskResponseDto(
        id = this.id,
        name = this.name,
        description = this.description,
        startDate = this.startDate,
        endDate = this.endDate,
        isDone = this.isDone
    )