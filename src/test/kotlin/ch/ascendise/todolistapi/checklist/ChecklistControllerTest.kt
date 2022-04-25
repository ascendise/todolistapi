package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserService
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
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

@SpringBootTest
@AutoConfigureMockMvc
class ChecklistControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var checklistService: ChecklistService
    @MockkBean
    private lateinit var userService: UserService

    private val jackson = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val user = User(id = 1, username = "user", email = "mail@domain.com")

    private val oidcUser = DefaultOidcUser(
        AuthorityUtils.createAuthorityList("SCOPE_message:read", "SCOPE_message:write"),
        OidcIdToken.withTokenValue("id-token")
            .claim("sub", "12345")
            .claim("email", user.email)
            .claim("given_name", user.username)
            .build())

    @BeforeEach
    fun setUp(){
        every { userService.getUser(oidcUser) } returns user
    }

    @Test
    fun `Fetch all checklists of user`() {
        val expectedChecklists = listOf(
            Checklist(id = 1, name = "New Checklist1", user = user),
            Checklist(id = 2, name = "New Checklist2", user = user),
            Checklist(id = 3, name = "New Checklist3", user = user))
        every { checklistService.getChecklists(user.id) } returns expectedChecklists
        val result = mockMvc.perform(
            get("/checklists")
                .with(oidcLogin().oidcUser(oidcUser))
        )
            .andExpect(status().isOk)
            .andReturn()
        verify { checklistService.getChecklists(user.id) }
        val checklists: List<Checklist> = jackson.readValue(result.response.contentAsString)
        assertEquals(expectedChecklists, checklists)
    }
}