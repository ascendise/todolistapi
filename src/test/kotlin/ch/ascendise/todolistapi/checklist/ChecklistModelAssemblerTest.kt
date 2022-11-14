package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.task.TaskModelAssembler
import ch.ascendise.todolistapi.task.TaskResponseDto
import ch.ascendise.todolistapi.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.server.mvc.linkTo

internal class ChecklistModelAssemblerTest
{
    private val user = User(id = 101, username = "User User", subject="auth|12345")
    private lateinit var checklistModelAssembler: ChecklistModelAssembler

    @BeforeEach
    fun setUp() {
        val taskModelAssembler = TaskModelAssembler()
        checklistModelAssembler = ChecklistModelAssembler(taskModelAssembler)
    }

    @Test
    fun `should return checklist with links to operations`() {
        val checklist = Checklist(id = 301, name = "Checklist1", user = user)
        val model = checklistModelAssembler.toModel(checklist)
        val expectedModel = ChecklistResponseDto(id = 101, name = "Checklist1", tasks = mutableListOf())
        expectedModel.add(
            linkTo<ChecklistController> { getChecklist(checklist.id, checklist.user) }.withSelfRel(),
            linkTo<ChecklistController> { getChecklists(checklist.user) }.withRel("checklists"),
            linkTo<ChecklistTaskController> { getRelations(checklist.user) }.withRel("relations")
        )
        assertEquals(expectedModel, model)
    }

    @Test
    fun `should include links with every task in checklist`() {
        val checklist = Checklist(id = 301, name = "Checklsit1", user = user, tasks = mutableListOf(
            Task(id = 201, name = "Task1", user = user),
            Task(id = 202, name = "Task2", user = user)
        ))
        val model = checklistModelAssembler.toModel(checklist)
        val expectedTaskModels = mutableListOf(
            TaskResponseDto(id = 201, name = "Task1").apply { addExpectedTasks(this, 301) },
            TaskResponseDto(id = 202, name = "Task2").apply { addExpectedTasks(this, 301)}
        )
        assertEquals(expectedTaskModels, model.tasks)
    }

    private fun addExpectedTasks(dto: TaskResponseDto, checklistId: Long) {
        dto.add(
            linkTo<TaskController> { getTask(user, dto.id) }.withSelfRel(),
            linkTo<TaskController> { getTasks(user) }.withRel("tasks"),
            linkTo<ChecklistTaskController> {
                removeRelation(user, checklistId, dto.id)
            }.withRel("removeTask")
        )
    }
}