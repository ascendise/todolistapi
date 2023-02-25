package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.task.TaskModelAssembler
import ch.ascendise.todolistapi.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Links
import org.springframework.hateoas.server.mvc.linkTo

internal class ChecklistModelAssemblerTest
{
    private val user = User(id = 101, subject="auth|12345")
    private lateinit var checklistModelAssembler: ChecklistModelAssembler

    @BeforeEach
    fun setUp() {
        val taskModelAssembler = TaskModelAssembler()
        checklistModelAssembler = ChecklistModelAssembler(taskModelAssembler)
    }

    @Test
    fun `toModel() should return checklist with links to operations`() {
        //Arrange
        val checklist = Checklist(id = 301, name = "Checklist1", user = user)
        //Act
        val model = checklistModelAssembler.toModel(checklist)
        //Assert
        var expectedLinks = listOf(
            linkTo<ChecklistController> { getChecklist(checklist.id, checklist.user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(checklist.user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(checklist.user) }.withRel("relations"),
            linkTo<ChecklistController> { complete(checklist.id, checklist.user)  }.withRel("complete")
        )
        assertEquals(expectedLinks, model.links.toList())
    }

    @Test
    fun `toModel() should include links with every task in checklist`() {
        //Arrange
        val checklist = Checklist(id = 301, name = "Checklsit1", user = user, tasks = mutableListOf(
            Task(id = 201, name = "Task1", user = user)
        ))
        //Act
        val model = checklistModelAssembler.toModel(checklist)
        //Assert
        val expectedTaskLinks = listOf(
            linkTo<TaskController> { getTask(user, checklist.tasks[0].id) }.withSelfRel(),
            linkTo<TaskController> { getTasks(user) }.withRel("tasks"),
            linkTo<ChecklistTaskController> {
                removeRelation(user, checklist.id, checklist.tasks[0].id)
            }.withRel("removeTask")
        )
        assertEquals(expectedTaskLinks, model.tasks[0].links.toList())
    }

    @Test
    fun `toModel() should include link to complete operation if all tasks are done`() {
        //Arrange
        val checklist = Checklist(id = 301, name = "Checklist1", user = user, tasks = mutableListOf(
            Task(id = 201, name = "Task1", user = user, isDone = true),
            Task(id = 202, name = "Task2", user = user, isDone = true)))
        //Act
        val model = checklistModelAssembler.toModel(checklist)
        //Assert
        val completeLink = model.links.getLink("complete").orElse(null)
        assertNotNull(completeLink)
        var expectedLink = linkTo<ChecklistController> { complete(checklist.id, user) }.withRel("complete")
        assertEquals(expectedLink, completeLink)
    }

    @Test
    fun `toModel() should not include link to complete operation if there are undone tasks`() {
        //Arrange
        val checklist = Checklist(id = 301, name = "Checklist1", user = user, tasks = mutableListOf(
            Task(id = 201, name = "Task1", user = user, isDone = false),
            Task(id = 202, name = "Task2", user = user, isDone = true)))
        //Act
        val model = checklistModelAssembler.toModel(checklist)
        //Assert
        val completeLink = model.links.getLink("complete").orElse(null)
        assertNull(completeLink)
    }
}