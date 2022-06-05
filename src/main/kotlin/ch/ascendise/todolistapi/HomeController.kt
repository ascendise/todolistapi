package ch.ascendise.todolistapi

import ch.ascendise.todolistapi.checklist.ChecklistController
import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.user.CurrentUser
import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
class HomeController {

    @GetMapping("/")
    fun getLinks(@CurrentUser user: User): EntityModel<Response>
    {
        val baseUri = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        return EntityModel.of(Response(),
            linkTo<TaskController> { getTasks(user) }.withRel("tasks"),
            linkTo<ChecklistController> { getChecklists(user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations"),
            linkTo<AuthorizationController> { login() }.withRel("login"),
            Link.of("$baseUri/logout", "logout")
        )
    }

    class Response
    {

    }
}