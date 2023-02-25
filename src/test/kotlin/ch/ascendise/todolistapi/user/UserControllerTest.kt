package ch.ascendise.todolistapi.user

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt

internal class UserControllerTest {

    private lateinit var controller: UserController

    private val userService = mockk<UserService>()
    private val userModelAssembler = UserModelAssembler()

    @BeforeEach
    fun setUp() {
        controller = UserController(userService, userModelAssembler)
    }

    @Test
    fun `should return user with links`() {
        val subject = "auth|12345"
        val jwt = mockk<Jwt>()
        every { jwt.subject } returns subject
        val expectedUser = User(id = 101, subject = subject)
        expectedUser.add(linkTo<UserController> { getCurrentUser(expectedUser) }.withSelfRel())
        expectedUser.add(linkTo<UserController> { getCurrentUser(expectedUser) }.withRel("user"))
        val user = User(id = 101, subject = subject)
        val actualUser = controller.getCurrentUser(user)
        assertEquals(expectedUser, actualUser)
    }

    @Test
    fun `should delete user and return a NO CONTENT status`() {
        val user =  User(id = 101, subject = "auth|12345")
        justRun { userService.delete(user) }
        val response = controller.deleteCurrentUser(user)
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertEquals(null, response.body)
        verify { userService.delete(user) }
    }

}