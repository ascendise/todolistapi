package ch.ascendise.todolistapi.user

import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class UserModelAssembler : RepresentationModelAssembler<User, User>{

    override fun toModel(user: User): User {
        user.add(linkTo<UserController> { getCurrentUser(user) }.withSelfRel())
        user.add(linkTo<UserController> { getCurrentUser(user) }.withRel("user"))
        return user
    }
}