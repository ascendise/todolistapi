package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @ResponseBody
    @ExceptionHandler(ChecklistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun checklistNotFoundException(): ResponseEntity<Any> =
        ResponseEntity.notFound().build()
}