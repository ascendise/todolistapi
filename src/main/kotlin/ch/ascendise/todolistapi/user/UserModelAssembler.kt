package ch.ascendise.todolistapi.user

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component

@Component
class UserModelAssembler : RepresentationModelAssembler<User, EntityModel<User>>{

    override fun toModel(user: User): EntityModel<User> =
        EntityModel.of(user,
            linkTo<UserController> { getCurrentUser(SecurityContextHolder.getContext().authentication.principal as OidcUser) }.withSelfRel(),
            linkTo<UserController> { getCurrentUser(SecurityContextHolder.getContext().authentication.principal as OidcUser) }.withRel("user")
        )

}