package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.task.Task
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class ChecklistControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockK
    private lateinit var jwt: Jwt
    @MockkBean
    private lateinit var checklistService: ChecklistService
    @MockkBean
    private lateinit var userService: UserService

    private val jackson = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")

    @BeforeEach
    fun setUp(){
        every { jwt.subject }.returns(user.subject)
        every { jwt.getClaimAsString("given_name") }.returns(user.username)
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf( "name" to user.username, "sub" to user.subject))
        every { userService.getUser(jwt) } returns user
    }

    @Test
    fun `Fetch all checklists of user`() {
        val returnedChecklists = listOf(
            Checklist(id = 101, name = "New Checklist1", user = user),
            Checklist(id = 102, name = "New Checklist2", user = user),
            Checklist(id = 103, name = "New Checklist3", user = user))
        every { checklistService.getChecklists(user.id) } returns returnedChecklists
        val result = mockMvc.perform(
            get("/checklists")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { checklistService.getChecklists(user.id) }
        val jsonNode = jackson.readTree(result.response.contentAsString)
        val checklists: List<ChecklistResponseDto> = jackson.treeToValue(jsonNode.at("/_embedded/checklists"))
        val expectedChecklists = returnedChecklists.map { it.toChecklistResponseDto() }.toList()
        assertEquals(expectedChecklists, checklists)
    }

    @Test
    fun `Fetch empty list of checklists`() {
        every { checklistService.getChecklists(user.id) } returns emptyList()
        val result = mockMvc.perform(
            get("/checklists")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { checklistService.getChecklists(user.id) }
        val node = jackson.readTree(result.response.contentAsString)
        assertTrue(node.findPath("/_embedded/checklists").isMissingNode)
    }

    @Test
    fun `Fetch single checklist`() {
        val returnedChecklist = Checklist(id = 100, name = "New Checklist", user = user)
        every { checklistService.getChecklist(returnedChecklist.id, user.id) } returns returnedChecklist
        val result = mockMvc.perform(
            get("/checklists/${returnedChecklist.id}")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { checklistService.getChecklist(returnedChecklist.id, user.id) }
        val checklist: ChecklistResponseDto = jackson.readValue(result.response.contentAsString)
        val expectedChecklist = returnedChecklist.toChecklistResponseDto()
        assertEquals(expectedChecklist, checklist)
    }

    @Test
    fun `Return 404 if checklist was not found`() {
        val id = -1L
        every { checklistService.getChecklist(id, user.id) } throws ChecklistNotFoundException()
        mockMvc.perform(
            get("/checklists/$id")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isNotFound)
            .andReturn()
        verify { checklistService.getChecklist(id, user.id) }
    }

    @Test
    fun `Create new checklist`() {
        val returnedChecklist = Checklist(id = 101, name = "ReadList", user = user)
        val checklistJson = "{\"name\":\"ReadList\"}"
        every { checklistService.create( match { it.name == "ReadList" } ) } returns returnedChecklist
        val result = mockMvc.perform(
            post("/checklists")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistJson)
                .contentType("application/json")
        )
            .andExpect(status().isCreated)
            .andReturn()
        verify { checklistService.create(any()) }
        val checklist: ChecklistResponseDto = jackson.readValue(result.response.contentAsString)
        val expectedChecklist = returnedChecklist.toChecklistResponseDto()
        assertEquals(expectedChecklist, checklist)
    }

    @Test
    fun `Update existing checklist`() {
        val checklistJson = "{\"name\":\"DescriptiveNameForCollectionOfTasks\"}"
        val returnedChecklist = Checklist(id = 101, name = "DescriptiveNameForCollectionOfTasks", user = user)
        every {
            checklistService.update(match { it.name == "DescriptiveNameForCollectionOfTasks" })
        } returns returnedChecklist
        val result = mockMvc.perform(
            put("/checklists/1")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistJson)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { checklistService.update(any()) }
        val checklist: ChecklistResponseDto = jackson.readValue(result.response.contentAsString)
        val expectedChecklist = returnedChecklist.toChecklistResponseDto()
        assertEquals(expectedChecklist, checklist)
    }

    @Test
    fun `Return 404 when trying to update nonexisting checklist`() {
        val checklistJson = "{\"name\":\"SomeChecklistName\"}"
        every { checklistService.update(any()) } throws ChecklistNotFoundException()
        mockMvc.perform(
            put("/checklists/-1")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistJson)
                .contentType("application/json")
        )
            .andExpect(status().isNotFound)
            .andReturn()
        verify { checklistService.update(any()) }
    }

    @Test
    fun `Delete checklist`() {
        val id = 101L
        every { checklistService.delete(id, user.id) } returns Unit
        mockMvc.perform(
            delete("/checklists/$id")
                .with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isNoContent)
            .andReturn()
        verify { checklistService.delete(id, user.id) }
    }

    @Test
    fun `Correct format for GET request`() {
        val task1 = Task(id = 201, name = "Task1", user = user)
        val task2 = Task(id = 202, name = "Task2", user = user)
        val task3 = Task(id = 203, name = "Task3", user = user)
        val expectedChecklists = listOf(
            Checklist(id = 101, name = "New Checklist1", user = user, tasks = mutableListOf(task1, task2)),
            Checklist(id = 102, name = "New Checklist2", user = user, tasks = mutableListOf(task3)))
        every { checklistService.getChecklists(user.id) } returns expectedChecklists
        mockMvc.perform(
            get("/checklists")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.self.href", Is.`is`("http://localhost/checklists/101")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0].tasks[0]._links.self.href", Is.`is`("http://localhost/tasks/201")))
            .andExpect(jsonPath("_embedded.checklists[0].tasks[0]._links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0].tasks[0]._links.removeTask.href", Is.`is`("http://localhost/checklists/101/tasks/201")))
            .andReturn()
    }

    @Test
    fun `Correct format for GET single checklist`() {
        val task1 = Task(id = 201, name = "Task1", user = user)
        val task2 = Task(id = 202, name = "Task2", user = user)
        val checklist = Checklist(id = 101, name = "New Checklist1", user = user, tasks = mutableListOf(task1, task2))
        every { checklistService.getChecklist(checklist.id, user.id) } returns checklist
        mockMvc.perform(
            get("/checklists/${checklist.id}")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/101")))
            .andExpect(jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("tasks[0]._links.self.href", Is.`is`("http://localhost/tasks/201")))
            .andExpect(jsonPath("tasks[0]._links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(jsonPath("tasks[0]._links.removeTask.href", Is.`is`("http://localhost/checklists/101/tasks/201")))
            .andReturn()
    }

    @Test
    fun `Correct format for POST checklist`() {
        val task1 = Task(id = 201, name = "Task1", user = user)
        val task2 = Task(id = 202, name = "Task2", user = user)
        val checklist = Checklist(id = 101, name = "New Checklist1", user = user, tasks = mutableListOf(task1, task2))
        val checklistJson = jackson.writeValueAsString(checklist)
        every { checklistService.create(any()) } returns checklist
        mockMvc.perform(
            post("/checklists/")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistJson)
                .contentType("application/json")
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/101")))
            .andExpect(jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andReturn()
    }

    @Test
    fun `Correct format for PUT checklist`() {
        val task1 = Task(id = 201, name = "Task1", user = user)
        val task2 = Task(id = 202, name = "Task2", user = user)
        val checklist = Checklist(id = 101, name = "New Checklist1", user = user, tasks = mutableListOf(task1, task2))
        val checklistJson = jackson.writeValueAsString(checklist)
        every { checklistService.update(any()) } returns checklist
        mockMvc.perform(
            put("/checklists/${checklist.id}")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(checklistJson)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/101")))
            .andExpect(jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andReturn()
    }
}