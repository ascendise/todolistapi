package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.task.TaskResponseDto
import ch.ascendise.todolistapi.task.toTaskResponseDto
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "checklists", itemRelation = "checklist")
open class ChecklistResponseDto(
    var id: Long = 0,
    var name: String,
    var tasks: MutableList<TaskResponseDto> = mutableListOf()
) : RepresentationModel<ChecklistResponseDto>()

fun Checklist.toChecklistResponseDto() =
    ChecklistResponseDto(
        id = this.id,
        name = this.name,
        tasks = this.tasks.stream()
            .map { it.toTaskResponseDto() }
            .toList()
    )
