package ch.ascendise.todolistapi.user

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) {

    fun getUser(jwt: Jwt) =
        userRepository.findBySubject(jwt.subject)

    fun delete(user: User) {
        userRepository.delete(user)
    }
}