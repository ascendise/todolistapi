package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChecklistTaskController(
    val service: ChecklistTaskService
) {

    @GetMapping("/checklists/tasks")
    fun getRelations(@CurrentUser user: User) =
        service.getRelations(user.id)

}