package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistRepository
import ch.ascendise.todolistapi.checklist.ChecklistResponseDto
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskRepository
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import javax.transaction.Transactional
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
internal class ChecklistTaskIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var checklistRepository: ChecklistRepository
    private lateinit var jackson: ObjectMapper
    private lateinit var jwt: Jwt
    private var user = User(id = 0, username = "Max Muster", subject = "auth|12345")
    private var checklist = Checklist(name = "My Checklist", user = user, tasks = mutableListOf(
        Task(name = "My Task 1", user = user),
        Task(name = "My Task 2", user = user)
    ))

    @BeforeEach
    fun setUp() {
        userRepository.save(user)
        taskRepository.saveAll(checklist.tasks)
        checklistRepository.save(checklist)
        jackson = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        jwt = getJwt()
    }

    fun getJwt(): Jwt {
        val jwt = mockk<Jwt>()
        every { jwt.subject }.returns(user.subject)
        every { jwt.getClaimAsString("name")}.returns(user.username)
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf( "name" to user.username, "sub" to user.subject))
        return jwt
    }

    @AfterEach
    fun tearDown() {
        checklistRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should return all relations between checklists and tasks on GET`() {
        val response = mockMvc.perform(
            get("/checklists/tasks").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andReturn()
        val json = jackson.readTree(response.response.contentAsString)
        val relationsJson = json.at("/_embedded/relations").toString()
        val relations: List<ChecklistTaskResponseDto> = jackson.readValue(relationsJson)
        val expectedRelations = listOf(
            ChecklistTaskResponseDto(checklist.id, checklist.tasks[0].id),
            ChecklistTaskResponseDto(checklist.id, checklist.tasks[1].id),
        )
        assertEquals(expectedRelations, relations)
    }

    @Test
    fun `should include operations for relation resource`() {
        mockMvc.perform(
            get("/checklists/tasks").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.relations[0]._links.checklist.href", Is.`is`("http://localhost/checklists/${checklist.id}")))
            .andExpect(jsonPath("_embedded.relations[0]._links.task.href", Is.`is`("http://localhost/tasks/${checklist.tasks[0].id}")))
            .andExpect(jsonPath("_embedded.relations[0]._links.removeTask.href", Is.`is`("http://localhost/checklists/${checklist.id}/tasks/${checklist.tasks[0].id}")))
            .andExpect(jsonPath("_embedded.relations[0]._links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.relations[1]._links.checklist.href", Is.`is`("http://localhost/checklists/${checklist.id}")))
            .andExpect(jsonPath("_embedded.relations[1]._links.task.href", Is.`is`("http://localhost/tasks/${checklist.tasks[1].id}")))
            .andExpect(jsonPath("_embedded.relations[1]._links.removeTask.href", Is.`is`("http://localhost/checklists/${checklist.id}/tasks/${checklist.tasks[1].id}")))
            .andExpect(jsonPath("_embedded.relations[1]._links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andReturn()
    }
}