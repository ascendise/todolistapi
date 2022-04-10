package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import javax.transaction.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var taskRepository: TaskRepository

    private lateinit var jackson: ObjectMapper

    private val user = User(username = "Reanu Keeves", email = "mail@domain.com")
    private val tasks = setOf(Task(name = "Buy bread", description = "Wholegrain", user = user),
        Task(name = "Do Taxes", startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(30), user = user)
    )

    @BeforeEach
    fun setUp() {
        userRepository.save(user)
        taskRepository.saveAll(tasks)
        jackson = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @AfterEach
    fun tearDown() {
        taskRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `Redirect for authentication if not logged in`() {
        mockMvc.perform(get("/tasks"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `Return tasks for user`() {
        val oidcUser = createOidcUser(user)
        val result = mockMvc.perform(
            get("/tasks").with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn()
        val json = jackson.readTree(result.response.contentAsString)
        val tasksJson = json.at("/_embedded/taskList").toString()
        val actualTasks: Set<Task> = jackson.readValue(tasksJson)
        assertTrue(actualTasks == tasks, "Did not return expected tasks")
    }

    fun createOidcUser(user: User): DefaultOidcUser = DefaultOidcUser(
        AuthorityUtils.createAuthorityList("SCOPE_message:read", "SCOPE_message:write"),
        OidcIdToken.withTokenValue("id-token")
            .claim("sub", "12345")
            .claim("email", user.email)
            .claim("given_name", user.username)
            .build()
    )

    @Test
    fun `Correct format for GET all request`() {
        val oidcUser = createOidcUser(user)
        val tasks = taskRepository.findAllByUserId(user.id)
        mockMvc.perform(
            get("/tasks").with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/tasks")))
            .andExpect(jsonPath("_embedded.taskList[0]._links.self.href",`is`("http://localhost/tasks/${tasks[0].id}")))
            .andExpect(jsonPath("_embedded.taskList[0]._links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `Return specific task for user`() {
        val oidcUser = createOidcUser(user)
        val expectedTask = taskRepository.findAllByUserId(user.id)[0]
        val result = mockMvc.perform(
            get("/tasks/${expectedTask.id}").with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().isOk)
            .andReturn()
        val actualTask: Task = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedTask, actualTask, "Returned task does not match expected task")
    }

    @Test
    fun `Correct format for GET request`() {
        val oidcUser = createOidcUser(user)
        val task = taskRepository.findAllByUserId(user.id)[0]
        mockMvc.perform(
            get("/tasks/${task.id}").with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href",`is`("http://localhost/tasks/${task.id}")))
            .andExpect(jsonPath("_links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `Correct format for POST request`() {
        val oidcUser = createOidcUser(user)
        val newTask = Task(
            name = "Clean bathroom",
            description = "Close attention to sink",
            startDate = LocalDate.now().plusDays(5),
            user = user)
        val json = jackson.writeValueAsString(newTask)
        val expectedId = taskRepository.findAll().last().id + 1
        mockMvc.perform(
            post("/tasks")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf())
                .content(json)
                .contentType("application/hal+json")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("_links.self.href",`is`("http://localhost/tasks/${expectedId}")))
            .andExpect(jsonPath("_links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `Return task on POST request`() {
        val newTask = Task(
            name = "Clean bathroom",
            description = "Close attention to sink",
            startDate = LocalDate.now().plusDays(5),
            user = user)
        val result = sendPOSTRequest(newTask)
            .andExpect(status().isCreated)
            .andReturn()
        val responseTask: Task = jackson.readValue(result.response.contentAsString)
        newTask.id = responseTask.id
        assertEquals(responseTask, newTask, "Returned task is not the one sent")
    }

    private fun sendPOSTRequest(task: Task): ResultActions {
        val oidcUser = createOidcUser(user)
        val json = jackson.writeValueAsString(task)
        return mockMvc.perform(
            post("/tasks")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf())
                .content(json)
                .contentType("application/json")
        )
    }

    @Test
    fun `Return error when creating task with startDate before today`() {
        val invalidTask = Task(
            name = "",
            startDate = LocalDate.now().minusDays(5),
            user = user
        )
        val result = sendPOSTRequest(invalidTask)
            .andExpect(status().isUnprocessableEntity)
            .andReturn()
        val response: ApiError = jackson.readValue(result.response.contentAsString)
        val expectedResponse = ApiError(
            statusCode = 422,
            name = "Unprocessable Entity",
            description = StartDateBeforeTodayTaskException().message
        )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `Return error when creating task with endDate before startDate`() {
        val invalidTask = Task(
            name = "Break time",
            description = "Let me finish this before I even started",
            startDate = LocalDate.now().plusMonths(1),
            endDate = LocalDate.now().plusDays(1),
            user = user
        )
        val result = sendPOSTRequest(invalidTask)
            .andExpect(status().isUnprocessableEntity)
            .andReturn()
        val response: ApiError = jackson.readValue(result.response.contentAsString)
        val expectedResponse = ApiError(
            statusCode = 422,
            name = "Unprocessable Entity",
            description = InvalidDateRangeTaskException().message
        )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `Change resource via PUT`() {
        val oldTask = taskRepository.findAll().first();
        val newTask = Task(id = oldTask.id, name = "Do something else", description = "Some description", user = user);
        sendPUTRequest(newTask, oldTask.id)
            .andExpect(status().isOk)
        val actualTask = taskRepository.findById(oldTask.id).get();
        assertEquals(actualTask, newTask);
    }

    private fun sendPUTRequest(task: Task, id: Long): ResultActions {
        val oidcUser = createOidcUser(user)
        val json = jackson.writeValueAsString(task)
        return mockMvc.perform(
            put("/tasks/$id")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf())
                .content(json)
                .contentType("application/json")
        )
    }

    @Test
    fun `Return PUT request in HAL format`() {
        val oldTask = taskRepository.findAll().first();
        val newTask = Task(id = oldTask.id, name = "Do something else", description = "Some description", user = user);
        sendPUTRequest(newTask, oldTask.id)
            .andExpect(status().isOk)
            .andExpect(jsonPath("_links.self.href",`is`("http://localhost/tasks/${oldTask.id}")))
            .andExpect(jsonPath("_links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `PUT request to nonexisting resource returns 404`() {
        val newTask = Task(id = 50000, name = "Do something else", description = "Some description", user = user);
        sendPUTRequest(newTask, 50000)
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Delete task`() {
        val task = tasks.elementAt(0)
        val oidcUser = createOidcUser(user)
        mockMvc.perform(
            delete("/tasks/${task.id}")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf())
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
        assertFalse(taskRepository.existsById(task.id))
    }
}