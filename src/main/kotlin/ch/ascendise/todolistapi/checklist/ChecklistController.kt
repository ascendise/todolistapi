package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.stream.Collectors

@RestController
@SecurityRequirement(name = "bearer-key")
class ChecklistController(
    val service: ChecklistService,
    val modelAssembler: ChecklistModelAssembler
) {

    @GetMapping("/checklists")
    fun getChecklists(@CurrentUser user: User): CollectionModel<ChecklistResponseDto> =
        service.getChecklists(user.id)
            .stream()
            .map { modelAssembler.toModel(it) }
            .collect(Collectors.toList())
            .let { CollectionModel.of(it,
                linkTo<ChecklistController> { getChecklists(user) }.withSelfRel(),
                linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations")) }

    @PostMapping("/checklists")
    fun create(@CurrentUser user: User, @RequestBody checklist: ChecklistRequestDto): ResponseEntity<ChecklistResponseDto> {
        val newChecklist = checklist.toChecklist(user)
            .let { service.create(it) }
            .let { modelAssembler.toModel(it)}
        return ResponseEntity
            .created(URI("/checklists/${newChecklist.id}"))
            .body(newChecklist)
    }

    @GetMapping("/checklists/{id}")
    fun getChecklist(@PathVariable id: Long, @CurrentUser user: User) =
        service.getChecklist(id, user.id)
            .let { modelAssembler.toModel(it) }

    @PutMapping("/checklists/{id}")
    fun update(@PathVariable id: Long, @CurrentUser user: User, @RequestBody dto: ChecklistRequestDto): ResponseEntity<ChecklistResponseDto> {
        val checklist = dto.toChecklist(user)
        checklist.id = id
        val newChecklist = service.update(checklist)
            .let { modelAssembler.toModel(it) }
        return ResponseEntity.ok(newChecklist)
    }

    @DeleteMapping("/checklists/{id}")
    fun delete(@PathVariable id: Long, @CurrentUser user: User): ResponseEntity<Any> {
        service.delete(id, user.id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/checklists/{id}/complete")
    fun complete(@PathVariable id: Long, @CurrentUser user: User): ResponseEntity<Any> {
        service.complete(id, user.id)
        return ResponseEntity.noContent().build()
    }

    @ResponseBody
    @ExceptionHandler(ChecklistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun checklistNotFoundException(): ResponseEntity<Any> =
        ResponseEntity.notFound().build()

    @ResponseBody
    @ExceptionHandler(ChecklistIncompleteException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun checklistIncompleteException(): ResponseEntity<ApiError> {
        var error = ApiError(
            statusCode = 400,
            name = "Bad Request",
            description = "Failed to complete checklist. Not all tasks are marked done. " +
                    "Did you mean to use query parameter 'force' or 'partial'?"
        )
        return ResponseEntity.badRequest().body(error)
    }
}