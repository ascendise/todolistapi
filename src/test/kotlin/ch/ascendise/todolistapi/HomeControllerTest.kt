package ch.ascendise.todolistapi

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.hamcrest.core.Is
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")

    @MockK
    private lateinit var jwt: Jwt

    @MockkBean
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp()
    {
        every { jwt.subject }.returns(user.subject)
        every { jwt.getClaimAsString("given_name") }.returns(user.username)
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf( "name" to user.username, "sub" to user.subject))
        every { userService.getUser(jwt) }.returns(user)
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
    }

    @Test
    @WithAnonymousUser
    fun `Return 404 for anonymous users`()
    {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/")
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

}