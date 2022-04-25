package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChecklistController(
    val service: ChecklistService
) {

    @GetMapping("/checklists")
    fun getChecklists(@CurrentUser user: User): List<Checklist> =
        service.getChecklists(user.id)
}