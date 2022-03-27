package ch.ascendise.todolistapi.user

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
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

    @MockkBean
    private lateinit var userRepository: UserRepository

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

    @Test
    fun `Show info of current user`() {
        val expectedUser = User(1, "maxmuster@mail.com", "Max Muster")
        every { userRepository.findByEmail(expectedUser.email) } returns expectedUser
        val oidcUser = DefaultOidcUser(
            AuthorityUtils.createAuthorityList("SCOPE_message:read"),
            OidcIdToken.withTokenValue("id-token")
                .claim("sub", "12345")
                .claim("email", expectedUser.email)
                .claim("given_name", expectedUser.username)
                .build()
        )
        val result = mockMvc.perform(
            get("/user").with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString
        assertAll({result.contains(expectedUser.email)},
            {result.contains(expectedUser.username)},
            {result.contains(expectedUser.id.toString())})
    }

}