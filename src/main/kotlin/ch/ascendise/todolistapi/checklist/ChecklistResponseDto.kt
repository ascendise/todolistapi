package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.task.Task
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "checklists", itemRelation = "checklist")
open class ChecklistResponseDto(
    var id: Long = 0,
    var name: String,
    var tasks: List<Task> = emptyList()
) : RepresentationModel<ChecklistResponseDto>() {

}

fun Checklist.toChecklistResponseDto() =
    ChecklistResponseDto(
        id = this.id,
        name = this.name,
        tasks = this.tasks
    )
