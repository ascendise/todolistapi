package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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

    private val user = User(username = "Reanu Keeves", email = "mail@domain.com")
    private val tasks = setOf(
        Task(name = "Buy bread", description = "Wholegrain", user = user),
        Task(name = "Do Taxes", startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(30), user = user)
    )

    @BeforeEach
    fun setUp() {
        userRepository.save(user)
        taskRepository.saveAll(tasks)
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
        taskRepository.deleteAll()
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
        val jackson = jacksonObjectMapper().registerModule(JavaTimeModule())
        val actualTasks: Set<Task> = jackson.readValue(result.response.contentAsString)
        assertTrue(actualTasks.equals(tasks), "Did not return expected tasks")
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
    fun `Return specific task for user`() {
        val oidcUser = createOidcUser(user)
        val expectedTask = taskRepository.findAllByUserId(user.id).get(0)
        val result = mockMvc.perform(
            get("/tasks/${expectedTask.id}").with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().isOk)
            .andReturn()
        val jackson = jacksonObjectMapper().registerModule(JavaTimeModule())
        val actualTask: Task = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedTask, actualTask, "Returned task does not match expected task")
    }
}