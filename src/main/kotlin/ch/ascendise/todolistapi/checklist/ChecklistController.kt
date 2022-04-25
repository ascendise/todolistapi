package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class ChecklistController(
    val service: ChecklistService
) {

    @GetMapping("/checklists")
    fun getChecklists(@CurrentUser user: User): List<Checklist> =
        service.getChecklists(user.id)

    @PostMapping("/checklists")
    fun create(@CurrentUser user: User, @RequestBody checklist: ChecklistDto): ResponseEntity<Checklist> {
        val newChecklist = checklist.toChecklist(user).let { service.create(it) }
        return ResponseEntity
            .created(URI("/checklists/${newChecklist.id}"))
            .body(newChecklist)
    }

    @GetMapping("/checklists/{id}")
    fun getChecklist(@PathVariable id: Long, @CurrentUser user: User) =
        service.getChecklist(id, user.id)

    @ResponseBody
    @ExceptionHandler(ChecklistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun checklistNotFoundException(): ResponseEntity<Any> =
        ResponseEntity.notFound().build()
}