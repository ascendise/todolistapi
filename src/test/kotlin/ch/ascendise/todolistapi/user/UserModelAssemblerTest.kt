package ch.ascendise.todolistapi.user

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.hateoas.Links

internal class UserModelAssemblerTest {

    private lateinit var modelAssembler: UserModelAssembler

    @BeforeEach
    fun setUp() {
        modelAssembler = UserModelAssembler()
    }

    @Test
    fun `should add links to user`() {
        val user = User(id = 101, subject = "auth|12345", username = "John Doe")
        val userWithLinks = modelAssembler.toModel(user)
        assertEquals(2, userWithLinks.links.count())
        assertEquals("/user", userWithLinks.links.getLink("self").get().href)
        assertEquals("/user", userWithLinks.links.getLink("user").get().href)
    }
}