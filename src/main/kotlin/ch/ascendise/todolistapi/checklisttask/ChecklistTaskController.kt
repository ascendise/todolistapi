package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.web.bind.annotation.GetMapping
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

}