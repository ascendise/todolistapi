package ch.ascendise.todolistapi.user

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.jwt.Jwt

internal class AudienceValidatorTest {

    @Test
    fun `validate(jwt) should return success if audience matches configured audience`() {
        //Arrange
        val validator = AudienceValidator("my-app-audience")
        val token = mockk<Jwt>();
        every { token.audience } returns listOf("my-app-audience")
        //Act
        val result = validator.validate(token)
        //Assert
        assertFalse(result.hasErrors())
    }

    @Test
    fun `validate(jwt) should return error if audience is not matching`() {
        //Arrange
        val validator = AudienceValidator("my-app-audience")
        val token = mockk<Jwt>()
        every { token.audience } returns listOf("wrong-app-audience")
        //Act
        val result = validator.validate(token)
        //Assert
        assertTrue(result.hasErrors())
        assertEquals(1, result.errors.size)
        val error = result.errors.first()
        assertEquals("invalid_token", error.errorCode)
        assertEquals("The required audience is missing", error.description)
    }

    @Test
    fun `validate(jwt) should return failure if token is null`() {
        //Arrange
        val validator = AudienceValidator("my-app-audience")
        //Act
        val result = validator.validate(null)
        //Assert
        assertTrue(result.hasErrors())
        assertEquals(1, result.errors.size)
        val error = result.errors.first()
        assertEquals("invalid_token", error.errorCode)
        assertEquals("The required audience is missing", error.description)
    }
}