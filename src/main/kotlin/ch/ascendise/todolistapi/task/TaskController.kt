package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.stream.Collectors
import javax.swing.text.html.parser.Entity


@RestController
class TaskController(
    val taskService: TaskService,
    val taskModelAssembler: TaskModelAssembler
) {

    @GetMapping("/tasks")
    fun getTasks(@CurrentUser user: User): CollectionModel<EntityModel<Task>> {
        val tasks = taskService.getAll(user.id).stream()
            .map { taskModelAssembler.toModel(it) }
            .collect(Collectors.toList())
        return CollectionModel.of(tasks,
            linkTo<TaskController> { getTasks(user) }.withSelfRel())
    }

    @GetMapping("/tasks/{id}")
    fun getTask(@CurrentUser user: User, @PathVariable id: Long): EntityModel<Task> =
        taskService.getById(user, id)
            .let { taskModelAssembler.toModel(it) }

    @PostMapping("/tasks")
    fun createTask(@CurrentUser user: User, @RequestBody task: Task): ResponseEntity<EntityModel<Task>>
    {
        task.user = user
        val responseBody = taskService.create(task)
            .let { taskModelAssembler.toModel(it) }
        return ResponseEntity
            .created(URI.create("/${responseBody.content?.id}"))
            .body(responseBody)
    }

    @PutMapping("/tasks/{id}")
    fun putTask(@CurrentUser user: User, @PathVariable id: Long, @RequestBody task: Task): ResponseEntity<EntityModel<Task>>
    {
        task.user = user
        val responseBody = taskService.update(task, id, user.id)
            .let {taskModelAssembler.toModel(it)}
        return ResponseEntity
            .ok()
            .body(responseBody)
    }

    @DeleteMapping("/tasks/{id}")
    fun deleteTask(@CurrentUser user: User, @PathVariable id: Long): ResponseEntity<Task>
    {
        taskService.delete(taskId = id, userId = user.id)
        return ResponseEntity
            .noContent()
            .build()
    }

    @ResponseBody
    @ExceptionHandler(InvalidTaskException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun invalidTaskHandler(ex: InvalidTaskException): ResponseEntity<ApiError> =
        ResponseEntity
            .unprocessableEntity()
            .body(ApiError(
                statusCode = 422,
                name = "Unprocessable Entity",
                description = ex.message ?: ""
            )
            )

    @ResponseBody
    @ExceptionHandler(TaskNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun taskNotFoundException(ex: TaskNotFoundException): ResponseEntity<ApiError> =
        ResponseEntity
            .notFound()
            .build()
}