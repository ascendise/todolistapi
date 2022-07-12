package ch.ascendise.todolistapi.user

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {

    fun getUser(jwt: Jwt) =
        userRepository.findByEmail(jwt.getClaimAsString("email"))

    fun delete(user: User) {
        userRepository.delete(user)
    }
}