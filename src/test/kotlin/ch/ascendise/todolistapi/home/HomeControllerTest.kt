package ch.ascendise.todolistapi.home

import ch.ascendise.todolistapi.HomeController
import ch.ascendise.todolistapi.checklist.ChecklistController
import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.linkTo

internal class HomeControllerTest {

    private val controller = HomeController()
    private val user = User(id = 101, username = "John Doe", subject = "auth|123456789")

    @Test
    fun `should return links to all resources`() {
        val response = controller.getLinks(user)
        val expectedResponse = EntityModel.of(Any(),
            linkTo<TaskController> { getTasks(user) }.withRel("tasks"),
            linkTo<ChecklistController> { getChecklists(user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations"),
            linkTo<UserController> { getCurrentUser(user) }.withRel("user"))
        assertEquals(expectedResponse.links, response.links)
    }
}