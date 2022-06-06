package ch.ascendise.todolistapi

import ch.ascendise.todolistapi.user.User
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerTest {

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

    @Test
    @WithAnonymousUser
    fun `Return all available oauth2 providers for login`(){
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("application/hal+json"))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.google.href", Is.`is`("http://localhost/login/google")))
            .andExpect(MockMvcResultMatchers.jsonPath("_links.self.href", Is.`is`("http://localhost/login")))
    }

    @Test
    fun `Login redirects to oauth2 provider`(){
        mockMvc.perform(get("/login/google"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithAnonymousUser
    fun `Logout redirects back to login`(){
        mockMvc.perform(post("/logout")
            .with(oidcLogin().oidcUser(oidcUser))
            .with(csrf())
        )
            .andExpect(status().isFound)
            .andExpect(redirectedUrl("/login"))
    }
}