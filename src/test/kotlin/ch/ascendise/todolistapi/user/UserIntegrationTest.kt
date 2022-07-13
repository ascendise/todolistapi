package ch.ascendise.todolistapi.user

import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.transaction.Transactional


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    @WithAnonymousUser
    fun `Return 404 if not authorized`()
    {
        mockMvc.perform(get("/user"))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithAnonymousUser
    fun `Return 404 when trying to access user that doesn't exist`()
    {
        mockMvc.perform(get("/user"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Show info of current user`() {
        val expectedUser = User(subject = "auth-oauth2|123451234512345", username = "Max Muster")
        userRepository.save(expectedUser)
        val jwt = getJwt(expectedUser)
        val result = mockMvc.perform(
            get("/user").with(jwt().jwt(jwt))
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString
        assertAll({result.contains(expectedUser.subject)},
            {result.contains(expectedUser.username)},
            {result.contains(expectedUser.id.toString())})
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
    fun `Delete user`() {
        val user = User(subject=  "auth-oauth2|123451234512345", username = "name")
        userRepository.save(user)
        val jwt = getJwt(user)
        mockMvc.perform(
            delete("/user").with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().is2xxSuccessful)
        assertEquals(0, userRepository.findAll().size, "User was not deleted")
    }

    @Test
    fun `Deleting user does not have a response body`() {
        val user = User(subject = "auth-oauth2|123451234512345", username = "name")
        userRepository.save(user)
        val jwt = getJwt(user)
        val result = mockMvc.perform(
            delete("/user").with(jwt().jwt(jwt))
                .with(csrf())
        )
            .andExpect(status().isNoContent)
            .andReturn()
        assertEquals("", result.response.contentAsString, "Response Body is not empty")
    }

    @Test
    fun `Show available operations for user`() {
        val expectedUser = User(subject = "auth-oauth2|123451234512345", username = "Max Muster")
        userRepository.save(expectedUser)
        val jwt = getJwt(expectedUser)
        mockMvc.perform(
            get("/user").with(jwt().jwt(jwt))
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/user")))
            .andExpect(jsonPath("_links.user.href", `is`("http://localhost/user")))
    }
}