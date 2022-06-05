package ch.ascendise.todolistapi

import io.mockk.confirmVerified
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

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
    @WithAnonymousUser
    fun `Login redirects to oauth2 provider`(){
        mockMvc.perform(get("/login/google"))
            .andExpect(status().is3xxRedirection)
    }
}