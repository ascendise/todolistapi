package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskController(
    val taskService: TaskService
) {

    @GetMapping("/tasks")
    fun getTasks(@CurrentUser user: User): Set<Task> = taskService.getAll(user.id)
}