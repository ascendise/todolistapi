package ch.ascendise.todolistapi.user

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Get redirected to Google OAuth2`()
    {
        mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithAnonymousUser()
    fun `Get redirected when not authenticated`()
    {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection)
    }

}