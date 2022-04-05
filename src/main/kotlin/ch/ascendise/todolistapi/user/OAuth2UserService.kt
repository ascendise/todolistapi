package ch.ascendise.todolistapi.user

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class OAuth2UserService(val userRepository: UserRepository) : OidcUserService() {

    override fun loadUser(userRequest: OidcUserRequest) : OidcUser {
        val oidcUser = super.loadUser(userRequest)
        val user = convertOidcUser(oidcUser)
        if(!userRepository.existsByEmail(user.email))
        {
            println("Created user")
            userRepository.save(user)
        }
        return oidcUser
    }

    private fun convertOidcUser(oidcUser: OidcUser): User {
        return User(
            email = oidcUser.attributes["email"] as String,
            username = oidcUser.attributes["given_name"] as String
        )
    }
}