package ch.ascendise.todolistapi

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
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
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val user = User(id = 100, username = "user", email = "mail@domain.com")

    private val oidcUser = DefaultOidcUser(
        AuthorityUtils.createAuthorityList("SCOPE_message:read", "SCOPE_message:write"),
        OidcIdToken.withTokenValue("id-token")
            .claim("sub", "12345")
            .claim("email", user.email)
            .claim("given_name", user.username)
            .build())

    @MockkBean
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp()
    {
        every { userService.getUser(oidcUser) }.returns(user)
    }

    @Test
    fun `Return available links`()
    {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/")
                .with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.tasks.href", Is.`is`("http://localhost/tasks")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.checklists.href", Is.`is`("http://localhost/checklists")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.relations.href", Is.`is`("http://localhost/checklists/tasks")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.login.href", Is.`is`("http://localhost/login")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.logout.href", Is.`is`("http://localhost/logout")))
    }

    @Test
    @WithAnonymousUser
    fun `Return available links for anonymous user`()
    {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/")
                .with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.login.href", Is.`is`("http://localhost/login"))
            )
    }

}