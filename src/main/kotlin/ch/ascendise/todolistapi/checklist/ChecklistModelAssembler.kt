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
): RepresentationModelAssembler<Checklist, Checklist> {

    override fun toModel(checklist: Checklist): Checklist {
        checklist.add(
            linkTo<ChecklistController> { getChecklist(checklist.id, checklist.user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(checklist.user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(checklist.user) }.withRel("relations")
        )
        checklist.tasks.stream()
            .map { taskModelAssembler.toModel(it) }
            .map { it.add(linkTo<ChecklistTaskController> {
                removeRelation(checklist.user, checklist.id, it.id)}.withRel("removeTask")) }
            .collect(Collectors.toList())
        checklist.user = userModelAssembler.toModel(User(checklist.user))
        return checklist
    }

}