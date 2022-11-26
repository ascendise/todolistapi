package ch.ascendise.todolistapi.home

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.transaction.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
internal class HomeIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository

    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")
    private val jwt = mockk<Jwt>()

    @BeforeEach
    fun setUp() {
        userRepository.save(user)
        setUpMockJwt()
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    private fun setUpMockJwt() {
        every { jwt.subject }.returns(user.subject)
        every { jwt.getClaimAsString("given_name") }.returns(user.username)
        every { jwt.hasClaim(any()) }.answers { callOriginal() }
        every { jwt.claims }.returns(mapOf("name" to user.username, "sub" to user.subject))
    }

    @Test
    fun `Return available links`()
    {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/")
                .with(jwt().jwt(jwt))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.user.href", Is.`is`("http://localhost/user")))
    }

    @Test
    @WithAnonymousUser
    fun `Return 401 for anonymous users`()
    {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

}