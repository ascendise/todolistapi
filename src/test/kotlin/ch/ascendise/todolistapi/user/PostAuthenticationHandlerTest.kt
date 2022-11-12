package ch.ascendise.todolistapi.user

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.jwt.Jwt

internal class PostAuthenticationHandlerTest
{
    private lateinit var postAuthHandler: PostAuthenticationHandler
    private val userRepository = mockk<UserRepository>()

    @BeforeEach
    fun setUp() {
        postAuthHandler = PostAuthenticationHandler(userRepository)
    }

    @Test
    fun `should create user if it does not exist yet`() {
        val event = mockk<AuthenticationSuccessEvent>()
        val jwt = mockk<Jwt>()
        every { event.authentication.principal } returns jwt
        every { jwt.getClaimAsString("name") } returns "John Doe"
        every { jwt.subject } returns "auth|12345"
        every { userRepository.existsBySubject("auth|12345") } returns false
        val expectedCreatedUser = User(id = 0, username = "John Doe", subject = "auth|12345")
        every { userRepository.save(expectedCreatedUser) } returns expectedCreatedUser
        postAuthHandler.onApplicationEvent(event)
        verifyAll {
            event.authentication.principal
            jwt.getClaimAsString("name")
            jwt.subject
            userRepository.existsBySubject("auth|12345")
            userRepository.save(expectedCreatedUser)
        }
    }

    @Test
    fun `should not create user if it already exists`() {
        val event = mockk<AuthenticationSuccessEvent>()
        val jwt = mockk<Jwt>()
        every { event.authentication.principal } returns jwt
        every { jwt.getClaimAsString("name") } returns "John Doe"
        every { jwt.subject } returns "auth|12345"
        every { userRepository.existsBySubject("auth|12345") } returns true
        val expectedCreatedUser = User(id = 0, username = "John Doe", subject = "auth|12345")
        every { userRepository.save(expectedCreatedUser) } returns expectedCreatedUser
        postAuthHandler.onApplicationEvent(event)
        verifyAll{
            event.authentication.principal
            jwt.getClaimAsString("name")
            jwt.subject
            userRepository.existsBySubject("auth|12345")
        }
    }
}