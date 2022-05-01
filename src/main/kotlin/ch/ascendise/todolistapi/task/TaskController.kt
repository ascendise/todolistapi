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


@RestController
class TaskController(
    val taskService: TaskService,
    val taskModelAssembler: TaskModelAssembler
) {

    @GetMapping("/tasks")
    fun getTasks(@CurrentUser user: User): CollectionModel<Task> {
        val tasks = taskService.getAll(user.id).stream()
            .map { taskModelAssembler.toModel(it) }
            .collect(Collectors.toList())
        return CollectionModel.of(tasks,
            linkTo<TaskController> { getTasks(user) }.withSelfRel())
    }

    @GetMapping("/tasks/{id}")
    fun getTask(@CurrentUser user: User, @PathVariable id: Long): Task =
        taskService.getById(user.id, id)
            .let { taskModelAssembler.toModel(it) }

    @PostMapping("/tasks")
    fun createTask(@CurrentUser user: User, @RequestBody taskDto: TaskDto): ResponseEntity<Task>
    {
        val task = taskDto.toTask(user)
        val responseBody = taskService.create(task)
            .let { taskModelAssembler.toModel(it) }
        return ResponseEntity
            .created(URI.create("/${responseBody.id}"))
            .body(responseBody)
    }

    @PutMapping("/tasks/{id}")
    fun putTask(@CurrentUser user: User, @PathVariable id: Long, @RequestBody taskDto: TaskDto): ResponseEntity<Task>
    {
        val task = taskDto.toTask(user)
        task.id = id
        val responseBody = taskService.update(task)
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