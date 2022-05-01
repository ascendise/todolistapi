package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.ChecklistController
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class ChecklistTaskModelAssembler: RepresentationModelAssembler<ChecklistTaskDto, ChecklistTaskDto> {

    override fun toModel(relation: ChecklistTaskDto): ChecklistTaskDto {
        val dummyUser = User(-1, "", "")
        return relation.add(
            linkTo<ChecklistController> { getChecklist(relation.checklistId, dummyUser) }.withRel("checklist"),
            linkTo<TaskController> { getTask(dummyUser, relation.taskId) }.withRel("task"),
            linkTo<ChecklistTaskController> { removeRelation(dummyUser, relation.checklistId, relation.taskId) }.withRel("removeTask"),
            linkTo<ChecklistTaskController> { getRelations(dummyUser) }.withRel("relations")
        )
    }

}