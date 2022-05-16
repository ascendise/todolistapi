package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.TaskModelAssembler
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserModelAssembler
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component
import java.util.stream.Collectors
@Component
class ChecklistModelAssembler(
    val taskModelAssembler: TaskModelAssembler,
    val userModelAssembler: UserModelAssembler
): RepresentationModelAssembler<Checklist, ChecklistResponseDto> {

    override fun toModel(checklist: Checklist): ChecklistResponseDto {
        val checklistDto = checklist.toChecklistResponseDto()
        checklistDto.add(
            linkTo<ChecklistController> { getChecklist(checklist.id, checklist.user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(checklist.user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(checklist.user) }.withRel("relations")
        )
        checklistDto.tasks.stream()
            .map { taskModelAssembler.toModel(it) }
            .map {
                it.add(linkTo<ChecklistTaskController> {
                    removeRelation(checklist.user, checklist.id, it.id)
                }.withRel("removeTask"))
            }
            .collect(Collectors.toList())
        return checklistDto
    }
}
