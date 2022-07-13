package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistNotFoundException
import ch.ascendise.todolistapi.checklist.ChecklistResponseDto
import ch.ascendise.todolistapi.checklist.toChecklistResponseDto
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskNotFoundException
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserService
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI

@SpringBootTest
@AutoConfigureMockMvc
class ChecklistTaskControllerTest {

    @MockK
    private lateinit var jwt: Jwt
    @MockkBean
    private lateinit var service: ChecklistTaskService
    @MockkBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val jackson = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")

    @BeforeEach
    fun setUp() {
        every { jwt.subject }.returns(user.subject)
        every { jwt.getClaimAsString("given_name") }.returns(user.username)
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf( "name" to user.username, "sub" to user.subject))
        every { userService.getUser(jwt) } returns user
    }

    @Test
    fun `Return relations as list`() {
        val expectedRelationDtos = listOf(
            ChecklistTaskResponseDto(checklistId = 301, taskId = 201),
            ChecklistTaskResponseDto(checklistId = 301, taskId = 202),
            ChecklistTaskResponseDto(checklistId = 301, taskId = 203),
            ChecklistTaskResponseDto(checklistId = 302, taskId = 202),
            ChecklistTaskResponseDto(checklistId = 302, taskId = 204)
        )
        val expectedRelations = mutableListOf<ChecklistTask>()
        for(relationDto in expectedRelationDtos) {
            expectedRelations.add(relationDto.toChecklistTask(user))
        }
        every { service.getRelations(user.id) } returns expectedRelations
        val result = mockMvc.perform(
            get("/checklists/tasks")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { service.getRelations(user.id) }
        val jsonNode = jackson.readTree(result.response.contentAsString)
        val relations: List<ChecklistTaskResponseDto> = jackson.treeToValue(jsonNode.at("/_embedded/relations"))
        assertEquals(expectedRelationDtos, relations)
    }

    @Test
    fun `Add task to checklist`() {
        val checklistTaskJson = "{\"checklistId\":301,\"taskId\":201}"
        val returnedChecklist = Checklist(
            id = 301, name = "Checklist", user = user, tasks = mutableListOf(
                Task(id = 201, name = "Task", user = user)
            )
        )
        every {
            service.addTask(ChecklistTask(301, 201, 100))
        } returns returnedChecklist
        val result = mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistTaskJson)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andReturn()
        val checklist: ChecklistResponseDto = jackson.readValue(result.response.contentAsString)
        verify {
            service.addTask(ChecklistTask(301, 201, user.id))
        }
        val expectedChecklist = returnedChecklist.toChecklistResponseDto()
        assertEquals(expectedChecklist, checklist)
    }

    @Test
    fun `Remove task from checklist`() {
        val expectedChecklist = Checklist(id =301, name = "Checklist", user = user)
        every { service.removeTask(ChecklistTask(301, 201, user.id)) } returns expectedChecklist
        val result = mockMvc.perform(
            delete(URI("/checklists/301/tasks/201"))
                .with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { service.removeTask(ChecklistTask(301, 201, user.id)) }
        val checklist: Checklist = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedChecklist, checklist)
    }

    @Test
    fun `Correct format for GET relations`() {
        val expectedRelationDtos = listOf(
            ChecklistTaskResponseDto(checklistId = 301, taskId = 201),
            ChecklistTaskResponseDto(checklistId = 301, taskId = 202))
        val expectedRelations = mutableListOf<ChecklistTask>()
        for(relationDto in expectedRelationDtos) {
            expectedRelations.add(relationDto.toChecklistTask(user))
        }
        every { service.getRelations(user.id) } returns expectedRelations
        mockMvc.perform(
            get("/checklists/tasks")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.relations[0]._links.checklist.href", Is.`is`("http://localhost/checklists/301")))
            .andExpect(jsonPath("_embedded.relations[0]._links.task.href", Is.`is`("http://localhost/tasks/201")))
            .andExpect(jsonPath("_embedded.relations[0]._links.removeTask.href", Is.`is`("http://localhost/checklists/301/tasks/201")))
            .andExpect(jsonPath("_embedded.relations[0]._links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andReturn()
    }

    @Test
    fun `Correct format for PUT task to checklist`() {
        val checklistTaskJson = "{\"checklistId\":301,\"taskId\":201}"
        val expectedChecklist = Checklist(
            id = 301, name = "Checklist", user = user, tasks = mutableListOf(
                Task(id = 201, name = "Task", user = user)
            )
        )
        every {
            service.addTask(ChecklistTask(301, 201, 100))
        } returns expectedChecklist
        mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistTaskJson)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/301")))
            .andExpect(jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("tasks[0]._links.self.href", Is.`is`("http://localhost/tasks/201")))
            .andExpect(jsonPath("tasks[0]._links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(jsonPath("tasks[0]._links.removeTask.href", Is.`is`("http://localhost/checklists/301/tasks/201")))
            .andReturn()
    }

    @Test
    fun `Trying to add nonexisting task returns 404`() {
        val checklistTaskJson = "{\"checklistId\":301,\"taskId\":201}"
        every {
            service.addTask(ChecklistTask(301, 201, 100))
        } throws TaskNotFoundException()
        val result = mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistTaskJson)
                .contentType("application/json")
        )
            .andExpect(status().isNotFound)
            .andReturn()
        val expectedError = ApiError(
            statusCode = 404,
            name = "Not Found",
            description = "Task could not be found"
        )
        val actualError: ApiError = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedError, actualError)
    }

    @Test
    fun `Trying to add task to nonexisting checklist returns 404`() {
        val checklistTaskJson = "{\"checklistId\":301,\"taskId\":201}"
        every {
            service.addTask(ChecklistTask(301, 201, 100))
        } throws ChecklistNotFoundException()
        val result = mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistTaskJson)
                .contentType("application/json")
        )
            .andExpect(status().isNotFound)
            .andReturn()
        val expectedError = ApiError(
            statusCode = 404,
            name = "Not Found",
            description = "Checklist could not be found"
        )
        val actualError: ApiError = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedError, actualError)
    }
}