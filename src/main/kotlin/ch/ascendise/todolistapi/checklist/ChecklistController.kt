package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ChecklistController(
    val service: ChecklistService
) {

    @GetMapping("/checklists")
    fun getChecklists(@CurrentUser user: User): List<Checklist> =
        service.getChecklists(user.id)

    @GetMapping("/checklists/{id}")
    fun getChecklist(@PathVariable id: Long, @CurrentUser user: User) =
        service.getChecklist(id, user.id)
}