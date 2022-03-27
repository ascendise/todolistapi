package ch.ascendise.todolistapi.user

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    val userRepository: UserRepository
) {

    @GetMapping("/user")
    fun getCurrentUser(@AuthenticationPrincipal oidcUser: OidcUser) : User? =
        userRepository.findByEmail(oidcUser.attributes["email"] as String)

    @DeleteMapping("/user")
    fun deleteCurrentUser(@AuthenticationPrincipal oidcUser: OidcUser) =
        userRepository.deleteByEmail(oidcUser.attributes["email"] as String ?: "")
}