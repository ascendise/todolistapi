package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChecklistTaskController(
    val service: ChecklistTaskService
) {

    @GetMapping("/checklists/tasks")
    fun getRelations(@CurrentUser user: User): List<ChecklistTaskDto> {
        val relations = mutableListOf<ChecklistTaskDto>()
        for(relation in service.getRelations(user.id)) {
            relations.add(ChecklistTaskDto(relation.checklistId, relation.taskId))
        }
        return relations
    }

    @PutMapping("/checklists/tasks")
    fun addRelation(@CurrentUser user: User, @RequestBody dto: ChecklistTaskDto) =
        service.addTask(dto.toChecklistTask(user))

    @DeleteMapping("/checklists/{checklistId}/tasks/{taskId}")
    fun removeRelation(@CurrentUser user: User,
                       @PathVariable checklistId: Long,
                       @PathVariable taskId: Long) =
        service.removeTask(ChecklistTask(checklistId, taskId, user.id))
}