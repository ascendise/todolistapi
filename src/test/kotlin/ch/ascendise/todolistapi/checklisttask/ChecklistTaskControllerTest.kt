package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserService
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.aspectj.lang.annotation.Before
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import kotlin.math.exp

@SpringBootTest
@AutoConfigureMockMvc
class ChecklistTaskControllerTest {

    @MockkBean
    private lateinit var service: ChecklistTaskService
    @MockkBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val jackson = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val user = User(id = 100, username = "user", email = "mail@domain.com")

    private val oidcUser = DefaultOidcUser(
        AuthorityUtils.createAuthorityList("SCOPE_message:read", "SCOPE_message:write"),
        OidcIdToken.withTokenValue("id-token")
            .claim("sub", "12345")
            .claim("email", user.email)
            .claim("given_name", user.username)
            .build())

    @BeforeEach
    fun setUp() {
        every { userService.getUser(oidcUser) } returns user
    }

    @Test
    fun `Return relations as list`() {
        val expectedRelationDtos = listOf(
            ChecklistTaskDto(checklistId = 301, taskId = 201),
            ChecklistTaskDto(checklistId = 301, taskId = 202),
            ChecklistTaskDto(checklistId = 301, taskId = 203),
            ChecklistTaskDto(checklistId = 302, taskId = 202),
            ChecklistTaskDto(checklistId = 302, taskId = 204)
        )
        val expectedRelations = mutableListOf<ChecklistTask>()
        for(relationDto in expectedRelationDtos) {
            expectedRelations.add(relationDto.toChecklistTask(user))
        }
        every { service.getRelations(user.id) } returns expectedRelations
        val result = mockMvc.perform(
            get("/checklists/tasks")
                .with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { service.getRelations(user.id) }
        val relations: List<ChecklistTaskDto> = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedRelationDtos, relations)
    }

    @Test
    fun `Add task to checklist`() {
        val checklistTaskJson = "{\"checklistId\":301,\"taskId\":201}"
        val expectedChecklist = Checklist(
            id = 301, name = "Checklist", user = user, tasks = mutableListOf(
                Task(id = 201, name = "Task", user = user)
            )
        )
        every {
            service.addTask(ChecklistTask(301, 201, 100))
        } returns expectedChecklist
        val result = mockMvc.perform(
            put("/checklists/tasks")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf())
                .content(checklistTaskJson)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andReturn()
        val checklist: Checklist = jackson.readValue(result.response.contentAsString)
        verify {
            service.addTask(ChecklistTask(301, 201, user.id))
            assertEquals(expectedChecklist, checklist)
        }
    }

    @Test
    fun `Remove task from checklist`() {
        val expectedChecklist = Checklist(id =301, name = "Checklist", user = user)
        every { service.removeTask(ChecklistTask(301, 201, user.id)) } returns expectedChecklist
        val result = mockMvc.perform(
            delete(URI("/checklists/301/tasks/201"))
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { service.removeTask(ChecklistTask(301, 201, user.id)) }
        val checklist: Checklist = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedChecklist, checklist)
    }
}