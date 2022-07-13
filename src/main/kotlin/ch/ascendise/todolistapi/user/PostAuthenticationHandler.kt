package ch.ascendise.todolistapi.user

import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class PostAuthenticationHandler(
    val userRepository: UserRepository
) : ApplicationListener<AuthenticationSuccessEvent> {

    override fun onApplicationEvent(event: AuthenticationSuccessEvent) {
        val jwt = event.authentication.principal as Jwt
        val user = getUserInfo(jwt)
        if(!userRepository.existsBySubject(user.subject))
        {
            userRepository.save(user)
        }
    }

    private fun getUserInfo(jwt: Jwt) = User(
        username = jwt.getClaimAsString("name"),
        subject = jwt.subject,
    )

}