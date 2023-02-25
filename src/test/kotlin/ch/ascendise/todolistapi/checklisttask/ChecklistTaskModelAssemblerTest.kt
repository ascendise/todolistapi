package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.ChecklistController
import ch.ascendise.todolistapi.checklist.ChecklistResponseDto
import ch.ascendise.todolistapi.task.TaskController
import ch.ascendise.todolistapi.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.hateoas.server.mvc.linkTo

internal class ChecklistTaskModelAssemblerTest
{

    private val modelAssembler = ChecklistTaskModelAssembler()

    @Test
    fun `should add links to entity`() {
        val dummyUser = User(-1, "")
        val checklistTask = ChecklistTaskResponseDto(taskId = 201, checklistId = 301)
        val checklistTaskWithLinks = modelAssembler.toModel(checklistTask)
        val expectedEntity = ChecklistTaskResponseDto(taskId = 201, checklistId = 301).apply {
            this.add(
                linkTo<ChecklistController> { getChecklist(301, dummyUser) }.withRel("checklist"),
                linkTo<TaskController> { getTask(dummyUser, 201) }.withRel("task"),
                linkTo<ChecklistTaskController> { removeRelation(dummyUser, 301, 201) }.withRel("removeTask"),
                linkTo<ChecklistTaskController> { getRelations(dummyUser) }.withRel("relations")
            )
        }
        assertEquals(expectedEntity, checklistTaskWithLinks)
    }
}