package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.checklisttask.ChecklistTaskController
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskResponseDto
import ch.ascendise.todolistapi.user.User
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.ResponseEntity
import java.net.URI

internal class ChecklistControllerTest
{
    private lateinit var controller: ChecklistController

    private val checklistService = mockk<ChecklistService>()
    private val checklistModelAssembler = mockk<ChecklistModelAssembler>();
    private val user = User(id = 101, subject = "auth|54321")

    @BeforeEach
    fun setUp() {
        controller = ChecklistController(checklistService, checklistModelAssembler)
    }

    @Test
    fun `getChecklists() should return list of checklists associated with user`() {
        //Arrange
        val checklists = listOf(
            Checklist(id = 301, name = "Checklist1", tasks = mutableListOf(
                Task(id = 201, name = "Task1", user = user),
                Task(id = 202, name = "Task2", user = user)
            ), user = user),
            Checklist(id = 302, name = "Checklist2", user = user)
        )
        every { checklistService.getChecklists(user.id) } returns checklists
        val expectedChecklists = listOf(
            ChecklistResponseDto(id = 301, name = "Checklist1", tasks = mutableListOf(
                TaskResponseDto(id = 201, name = "Task1"),
                TaskResponseDto(id = 202, name = "Task2")
            )),
            ChecklistResponseDto(id = 302, name = "Checklist2")
        )
        every { checklistModelAssembler.toModel(checklists[0])} returns expectedChecklists[0]
        every { checklistModelAssembler.toModel(checklists[1])} returns expectedChecklists[1]
        //Act
        val response = controller.getChecklists(user)
        val expectedResponse = CollectionModel.of(expectedChecklists,
            linkTo<ChecklistController> { getChecklists(user) }.withSelfRel(),
            linkTo<ChecklistTaskController> { getRelations(user) }.withRel("relations"),
        )
        //Assert
        assertEquals(expectedResponse, response)
        verify(exactly = 1) { checklistService.getChecklists(user.id) }
        verify(exactly = 1) { checklistModelAssembler.toModel(checklists[1])}
        verify(exactly = 1) { checklistModelAssembler.toModel(checklists[0])}
    }

    @Test
    fun `getChecklist() should return specified checklist`() {
        //Arrange
        val checklistId = 301L
        val checklist = Checklist(id = 301, name = "Checklist1", user = user, tasks = mutableListOf(
            Task(id = 201, name = "Task1", user = user),
            Task(id = 202, name = "Task2", user = user)
        ))
        every { checklistService.getChecklist(checklistId, user.id) } returns checklist
        val expectedResponse = ChecklistResponseDto(id = 301 , name = "Checklist1", tasks = mutableListOf(
            TaskResponseDto(id = 201, name = "Task1"),
            TaskResponseDto(id = 202, name = "Task2")
        ))
        every { checklistModelAssembler.toModel(checklist) } returns expectedResponse
        //Act
        val response = controller.getChecklist(checklistId, user)
        //Assert
        assertEquals(expectedResponse, response)
        verify(exactly = 1) { checklistService.getChecklist(checklistId, user.id) }
        verify(exactly = 1) { checklistModelAssembler.toModel(checklist) }
    }

    @Test
    fun `getChecklist() should throw ChecklistNotFoundException when getting nonexisting checklist`() {
        //Arrange
        val checklistId = 999L
        every { checklistService.getChecklist(checklistId, user.id) } throws ChecklistNotFoundException()
        //Act //Assert
        assertThrows<ChecklistNotFoundException> { controller.getChecklist(checklistId, user) }
        verify(exactly = 1) { checklistService.getChecklist(checklistId, user.id) }
    }

    @Test
    fun `create() should create Checklist and return the created entity`() {
        //Arrange
        every { checklistService.create(Checklist(name = "Checklist1", user = user)) } returnsArgument 0
        val expectedChecklist = ChecklistResponseDto(id = 0, name = "Checklist1")
        every { checklistModelAssembler.toModel(any()) } returns expectedChecklist
        val request = ChecklistRequestDto(name = "Checklist1")
        //Act
        val response = controller.create(user, request)
        //Assert
        val expectedResponse = ResponseEntity
            .created(URI.create("/checklists/0"))
            .body(expectedChecklist)
        assertEquals(expectedResponse, response)
        verify(exactly = 1) { checklistService.create(Checklist(name = "Checklist1", user = user)) }
        verify(exactly = 1) { checklistModelAssembler.toModel(any()) }
    }

    @Test
    fun `update() should update checklist with given id and return updated checklist`() {
        //Arrange
        every { checklistService.update(Checklist(id = 301, name = "Checklist2", user = user)) } returnsArgument 0
        val expectedChecklist = ChecklistResponseDto(id = 301, name = "Checklist2")
        every { checklistModelAssembler.toModel(any()) } returns expectedChecklist
        val checklistId = 301L
        val request = ChecklistRequestDto(name = "Checklist2")
        //Act
        val response = controller.update(checklistId, user, request)
        val expectedResponse = ResponseEntity.ok(expectedChecklist)
        //Arrange
        assertEquals(expectedResponse, response)
        verify(exactly = 1) { checklistService.update(Checklist(id = 301, name = "Checklist2", user = user)) }
        verify(exactly = 1) { checklistModelAssembler.toModel(any()) }
    }

    @Test
    fun `update() should throw ChecklistNotFoundException when trying to update nonexisting checklist`() {
        //Arrange
        val checklistId = 999L
        every {
            checklistService.update(Checklist(id = 999, name = "Checklist2", user = user))
        } throws ChecklistNotFoundException()
        //Act
        val request = ChecklistRequestDto(name = "Checklist2")
        //Assert
        assertThrows<ChecklistNotFoundException> { controller.update(checklistId, user, request) }
        verify { checklistService.update(Checklist(id = 999, name = "Checklist2", user = user)) }
    }

    @Test
    fun `delete() should delete specified checklist and return a No Content response`() {
        //Arrange
        val checklistId = 301L
        justRun { checklistService.delete(checklistId, user.id) }
        //Act
        val response = controller.delete(checklistId, user)
        //Assert
        val expectedResponse = ResponseEntity.noContent().build<Any>()
        assertEquals(expectedResponse, response)
        verify { checklistService.delete(checklistId, user.id) }
    }

    @Test
    fun `checklistNotFoundException() should return 'Not Found'`() {
        //Act
        val response = controller.checklistNotFoundException()
        //Assert
        val expectedResponse = ResponseEntity.notFound().build<Any>()
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `complete() should complete checklist and return no content`() {
        //Arrange
        val checklistId = 301L
        justRun { checklistService.complete(checklistId, user.id) }
        //Act
        val response = controller.complete(checklistId, user);
        //Assert
        val expectedResponse = ResponseEntity.noContent().build<Any>()
        assertEquals(expectedResponse, response)
        verify { checklistService.complete(checklistId, user.id) }
    }
}
