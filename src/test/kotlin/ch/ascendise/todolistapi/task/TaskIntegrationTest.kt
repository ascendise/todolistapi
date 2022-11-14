package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import javax.transaction.Transactional

@SpringBootTest
@AutoConfigureMockMvc
internal class TaskIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    private lateinit var jackson: ObjectMapper
    private val user = User(username = "Reanu Keeves", subject = "auth-oauth2|123451234512345")
    private val otherUser = User(username = "AidenPierce", subject = "auth-oauth2|543215432154321")
    private val tasks = setOf(Task(name = "Buy bread", description = "Wholegrain", user = user),
        Task(name = "Do Taxes", startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(30), user = user)
    )

    @BeforeEach
    fun setUp() {
        userRepository.save(user)
        userRepository.save(otherUser)
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
    fun `should return 401 if not authorized`() {
        mockMvc.perform(get("/tasks"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should return tasks for user`() {
        val jwt = getJwt(user)
        val result = mockMvc.perform(
            get("/tasks").with(jwt().jwt(jwt))
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn()
        val json = jackson.readTree(result.response.contentAsString)
        val tasksJson = json.at("/_embedded/tasks").toString()
        val actualTasks: List<TaskResponseDto> = jackson.readValue(tasksJson)
        val expectedTasks = tasks.stream()
            .map { it.toTaskResponseDto() }
            .toList()
        assertEquals(actualTasks, expectedTasks, "Did not return expected tasks")
    }

    fun getJwt(user: User): Jwt {
        val jwt = mockk<Jwt>()
        every { jwt.subject }.returns(user.subject)
        every { jwt.getClaimAsString("name")}.returns(user.username)
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf( "name" to user.username, "sub" to user.subject))
        return jwt
    }

    @Test
    fun `should return links to possible actions when GETting all tasks`() {
        val jwt = getJwt(user)
        val tasks = taskRepository.findAllByUserId(user.id)
        mockMvc.perform(
            get("/tasks").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/tasks")))
            .andExpect(jsonPath("_embedded.tasks[0]._links.self.href",`is`("http://localhost/tasks/${tasks[0].id}")))
            .andExpect(jsonPath("_embedded.tasks[0]._links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `should return specific task for user`() {
        val jwt = getJwt(user)
        val returnedTask = taskRepository.findAllByUserId(user.id)[0]
        val result = mockMvc.perform(
            get("/tasks/${returnedTask.id}").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        val actualTask: TaskResponseDto = jackson.readValue(result.response.contentAsString)
        val expectedTask = returnedTask.toTaskResponseDto()
        assertEquals(expectedTask, actualTask, "Returned task does not match expected task")
    }

    @Test
    fun `should return links to possible operations when GETting single task`() {
        val jwt = getJwt(user)
        val task = taskRepository.findAllByUserId(user.id)[0]
        mockMvc.perform(
            get("/tasks/${task.id}").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href",`is`("http://localhost/tasks/${task.id}")))
            .andExpect(jsonPath("_links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `should return possible operations for task on POST request`() {
        val jwt = getJwt(user)
        val newTask = Task(
            name = "Clean bathroom",
            description = "Close attention to sink",
            startDate = LocalDate.now().plusDays(5),
            user = user)
        val json = jackson.writeValueAsString(newTask)
        val expectedId = taskRepository.findAll().last().id + 1
        mockMvc.perform(
            post("/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(json)
                .contentType("application/hal+json")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("_links.self.href",`is`("http://localhost/tasks/${expectedId}")))
            .andExpect(jsonPath("_links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `should return created task on POST request`() {
        val newTask = Task(
            name = "Clean bathroom",
            description = "Close attention to sink",
            startDate = LocalDate.now().plusDays(5),
            user = user)
        val result = sendPOSTRequest(newTask)
            .andExpect(status().isCreated)
            .andReturn()
        val responseTask: TaskResponseDto = jackson.readValue(result.response.contentAsString)
        newTask.id = responseTask.id
        val expectedTask = newTask.toTaskResponseDto()
        assertEquals(expectedTask, responseTask, "Returned task is not the one sent")
    }

    private fun sendPOSTRequest(task: Task): ResultActions {
        val jwt = getJwt(user)
        val json = jackson.writeValueAsString(task)
        return mockMvc.perform(
            post("/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(json)
                .contentType("application/json")
        )
    }

    @Test
    fun `should return an error message when trying to create task with start date before today`() {
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
    fun `should return error when creating task with endDate before startDate`() {
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
    fun `should only have to set important stuff in POST`() {
        val jwt = getJwt(user)
        val taskJson = "{\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"Clean bathroom\",\n" +
                "    \"description\": \"Close attention to sink\",\n" +
                "    \"startDate\": [\n" +
                "        2040,\n" +
                "        4,\n" +
                "        16\n" +
                "    ],\n" +
                "    \"endDate\": null\n" +
                "}"
        mockMvc.perform(
            post("/tasks")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(taskJson)
                .contentType("application/json")
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `should change resource via PUT`() {
        val oldTask = taskRepository.findAll().first()
        val newTask = Task(id = oldTask.id, name = "Do something else", description = "Some description", user = user)
        sendPUTRequest(newTask, oldTask.id)
            .andExpect(status().isOk)
        val actualTask = taskRepository.findById(oldTask.id).get()
        assertEquals(actualTask, newTask)
    }

    private fun sendPUTRequest(task: Task, id: Long): ResultActions {
        val jwt = getJwt(user)
        val json = jackson.writeValueAsString(task)
        return mockMvc.perform(
            put("/tasks/$id")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(json)
                .contentType("application/json")
        )
    }

    @Test
    fun `should return PUT request in HAL format`() {
        val oldTask = taskRepository.findAll().first()
        val newTask = Task(id = oldTask.id, name = "Do something else", description = "Some description", user = user)
        sendPUTRequest(newTask, oldTask.id)
            .andExpect(status().isOk)
            .andExpect(jsonPath("_links.self.href",`is`("http://localhost/tasks/${oldTask.id}")))
            .andExpect(jsonPath("_links.tasks.href",`is`("http://localhost/tasks")))
    }

    @Test
    fun `should return 404 when sending PUT request to nonexisting resource`() {
        val newTask = Task(id = 50000, name = "Do something else", description = "Some description", user = user)
        sendPUTRequest(newTask, 50000)
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should only have to set important stuff in PUT`() {
        val jwt = getJwt(user)
        val taskJson = "{\n" +
                "    \"id\": 0,\n" +
                "    \"name\": \"Clean bathroom\",\n" +
                "    \"description\": \"Close attention to sink\",\n" +
                "    \"startDate\": [\n" +
                "        2040,\n" +
                "        4,\n" +
                "        16\n" +
                "    ],\n" +
                "    \"endDate\": null\n" +
                "}"
        val oldTask = taskRepository.findAll().first()
        mockMvc.perform(
            put("/tasks/${oldTask.id}")
                .with(jwt().jwt(jwt))
                .with(csrf())
                .content(taskJson)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should delete task when sending DELETE request`() {
        val task = tasks.elementAt(0)
        val jwt = getJwt(user)
        mockMvc.perform(
            delete("/tasks/${task.id}")
                .with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
        assertFalse(taskRepository.existsById(task.id))
    }

    @Test
    fun `should return 404 when trying to get task from other user`()
    {
        val jwt = getJwt(otherUser)
        val task = tasks.elementAt(0)
        mockMvc.perform(
            get("/tasks/${task.id}")
                .with(jwt().jwt(jwt))
            )
            .andExpect(status().isNotFound)
            .andExpect(content().string(""))
    }

    @Test
    fun `should return 404 when trying to update a resource from another user`() {
        val jwt = getJwt(otherUser)
        val task = tasks.elementAt(0)
        val newTask = Task(name = "pwned", description = "This is my task now", user = otherUser)
        mockMvc.perform(
            put("/tasks/${task.id}")
                .content(jackson.writeValueAsString(newTask))
                .contentType("application/json")
                .with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isNotFound)
            .andExpect(content().string(""))
        val actualTask = taskRepository.findById(task.id).get()
        assertEquals(task, actualTask)
    }

    @Test
    fun `should return 204 when trying to delete a resource from another user but don't actually delete it`() {
        val jwt = getJwt(otherUser)
        val task = tasks.elementAt(0)
        mockMvc.perform(
            delete("/tasks/${task.id}")
                .with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
        assertTrue(taskRepository.existsById(task.id))
    }
}