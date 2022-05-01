package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.ChecklistModelAssembler
import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
class ChecklistTaskController(
    val service: ChecklistTaskService,
    val checklistTaskModelAssembler: ChecklistTaskModelAssembler,
    val checklistModelAssembler: ChecklistModelAssembler
) {

    @GetMapping("/checklists/tasks")
    fun getRelations(@CurrentUser user: User): CollectionModel<ChecklistTaskDto> =
        service.getRelations(user.id)
            .stream()
            .map { ct -> ChecklistTaskDto(ct.checklistId, ct.taskId).let { checklistTaskModelAssembler.toModel(it) } }
            .collect(Collectors.toList())
            .let { CollectionModel.of(it,
                linkTo<ChecklistTaskController> { getRelations(user) }.withSelfRel(),
                linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations")) }

    @PutMapping("/checklists/tasks")
    fun addRelation(@CurrentUser user: User, @RequestBody dto: ChecklistTaskDto) =
        service.addTask(dto.toChecklistTask(user))
            .let { checklistModelAssembler.toModel(it) }

    @DeleteMapping("/checklists/{checklistId}/tasks/{taskId}")
    fun removeRelation(@CurrentUser user: User,
                       @PathVariable checklistId: Long,
                       @PathVariable taskId: Long) =
        service.removeTask(ChecklistTask(checklistId, taskId, user.id))
}