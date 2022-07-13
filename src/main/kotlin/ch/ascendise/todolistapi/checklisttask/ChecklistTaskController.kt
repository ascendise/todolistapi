package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.checklist.ChecklistModelAssembler
import ch.ascendise.todolistapi.checklist.ChecklistNotFoundException
import ch.ascendise.todolistapi.task.TaskNotFoundException
import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@RestController
@SecurityRequirement(name = "bearer-key")
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

    @ResponseBody
    @ExceptionHandler(TaskNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun taskNotFoundException(): ResponseEntity<Any> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(statusCode = 404, name = "Not Found", description = "Task could not be found"))

    @ResponseBody
    @ExceptionHandler(ChecklistNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun checklistNotFoundException(): ResponseEntity<Any> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(statusCode = 404, name = "Not Found", description = "Checklist could not be found"))
}