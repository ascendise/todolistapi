package ch.ascendise.todolistapi.checklisttask

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistRepository
import ch.ascendise.todolistapi.checklist.ChecklistResponseDto
import ch.ascendise.todolistapi.checklist.toChecklistResponseDto
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskRepository
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import javax.transaction.Transactional
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
internal class ChecklistTaskIT {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var checklistRepository: ChecklistRepository
    @MockkBean private lateinit var jwtDecoder: JwtDecoder
    private lateinit var jackson: ObjectMapper
    private lateinit var jwt: Jwt
    private var user = User(id = 0, subject = "auth|12345")
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
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf("sub" to user.subject))
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
    fun `should include operations for relation resource on GET`() {
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

    @Test
    fun `should add task to checklist and return the updated checklist`() {
        val newTask = taskRepository.save(Task(name = "Another task", user = user))
        val addRequest = ChecklistTaskRequestDto(checklistId = checklist.id, newTask.id)
        val jsonRequest = jackson.writeValueAsString(addRequest)
        val response = mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(jsonRequest)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andReturn()
        val updatedChecklist: ChecklistResponseDto = jackson.readValue(response.response.contentAsString)
        val updatedChecklistInDb = checklistRepository.findByIdAndUserId(checklist.id, user.id).get()
        assertEquals(updatedChecklist, updatedChecklistInDb.toChecklistResponseDto())
        assertEquals(3, updatedChecklistInDb.tasks.count())
        assertTrue(updatedChecklistInDb.tasks.contains(newTask))
    }

    @Test
    fun `should return updated checklist with added task as HATEOAS entity`() {
        val newTask = taskRepository.save(Task(name = "Another task", user = user))
        val addRequest = ChecklistTaskRequestDto(checklistId = checklist.id, newTask.id)
        val jsonRequest = jackson.writeValueAsString(addRequest)
        mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(jsonRequest)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", Is.`is`("http://localhost/checklists/${checklist.id}")))
            .andExpect(jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("tasks[0]._links.self.href", Is.`is`("http://localhost/tasks/${checklist.tasks[0].id}")))
            .andExpect(jsonPath("tasks[0]._links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(jsonPath("tasks[0]._links.removeTask.href",
                Is.`is`("http://localhost/checklists/${checklist.id}/tasks/${checklist.tasks[0].id}")
            ))
            .andExpect(jsonPath("tasks[1]._links.self.href", Is.`is`("http://localhost/tasks/${checklist.tasks[1].id}")))
            .andExpect(jsonPath("tasks[1]._links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(jsonPath("tasks[1]._links.removeTask.href",
                Is.`is`("http://localhost/checklists/${checklist.id}/tasks/${checklist.tasks[1].id}")
            ))
            .andExpect(jsonPath("tasks[2]._links.self.href", Is.`is`("http://localhost/tasks/${checklist.tasks[2].id}")))
            .andExpect(jsonPath("tasks[2]._links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(jsonPath("tasks[2]._links.removeTask.href",
                Is.`is`("http://localhost/checklists/${checklist.id}/tasks/${checklist.tasks[2].id}")
            ))
    }

    @Test
    fun `should return 404 and error response body if task to be added does not exist in DB`() {
        val addRequest = ChecklistTaskRequestDto(checklistId = checklist.id, 999)
        val jsonRequest = jackson.writeValueAsString(addRequest)
        val response = mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(jsonRequest)
                .contentType("application/json")
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType("application/json"))
            .andReturn()
        val expectedError = ApiError(
            statusCode = 404, name = "Not Found", description = "Task could not be found")
        val error: ApiError = jackson.readValue(response.response.contentAsString)
        assertEquals(expectedError, error)
    }


    @Test
    fun `should return 404 and error response body if checklist to be updated does not exist in DB`() {
        val newTask = taskRepository.save(Task(name = "Another task", user = user))
        val addRequest = ChecklistTaskRequestDto(checklistId = 999, taskId = newTask.id)
        val jsonRequest = jackson.writeValueAsString(addRequest)
        val response = mockMvc.perform(
            put("/checklists/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(jsonRequest)
                .contentType("application/json")
        )
            .andExpect(status().isNotFound)
            .andExpect(content().contentType("application/json"))
            .andReturn()
        val expectedErrorMessage = ApiError(
            statusCode = 404, name = "Not Found", description = "Checklist could not be found")
        val errorMessage: ApiError = jackson.readValue(response.response.contentAsString)
        assertEquals(expectedErrorMessage, errorMessage)
    }

    @Test
    fun `should remove task from checklist and return updated checklist`() {
        val taskToBeRemoved = checklist.tasks.first()
        val response = mockMvc.perform(
            delete("/checklists/${checklist.id}/tasks/${taskToBeRemoved.id}")
                .with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andReturn()
        val updatedChecklist: Checklist = jackson.readValue(response.response.contentAsString)
        val updatedChecklistInDb = checklistRepository.findById(checklist.id).get()
        assertFalse(taskToBeRemoved in updatedChecklistInDb.tasks)
        assertEquals(updatedChecklist, updatedChecklistInDb)
    }

}