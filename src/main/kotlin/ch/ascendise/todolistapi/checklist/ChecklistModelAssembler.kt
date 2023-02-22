package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.TaskModelAssembler
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class ChecklistModelAssembler(
    val taskModelAssembler: TaskModelAssembler
): RepresentationModelAssembler<Checklist, ChecklistResponseDto> {

    override fun toModel(checklist: Checklist): ChecklistResponseDto {
        val checklistDto = checklist.toChecklistResponseDto()
        checklistDto.add(
            linkTo<ChecklistController> { getChecklist(checklist.id, checklist.user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(checklist.user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(checklist.user) }.withRel("relations")
        )
        if(allTasksAreDone(checklist))
        {
            var completionLink = linkTo<ChecklistController> { complete(checklist.id, checklist.user) }
                .withRel("complete")
            checklistDto.add(completionLink)
        }
        checklistDto.tasks.stream()
            .map { taskModelAssembler.toModel(it, checklist.user) }
            .map {
                it.add(linkTo<ChecklistTaskController> {
                    removeRelation(checklist.user, checklist.id, it.id)
                }.withRel("removeTask"))
            }
            .collect(Collectors.toList())
        return checklistDto
    }

    fun allTasksAreDone(checklist: Checklist): Boolean
        = checklist.tasks.all { it.isDone }
}
