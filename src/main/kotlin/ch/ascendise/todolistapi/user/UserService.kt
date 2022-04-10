package ch.ascendise.todolistapi.user

import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {

    fun getUser(oidcUser: OidcUser) =
        userRepository.findByEmail(oidcUser.attributes["email"] as String)

    fun delete(user: User) {
        userRepository.delete(user)
    }
}