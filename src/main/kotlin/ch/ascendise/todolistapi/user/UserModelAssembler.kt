package ch.ascendise.todolistapi.user

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class UserModelAssembler : RepresentationModelAssembler<User, EntityModel<User>>{

    override fun toModel(user: User): EntityModel<User> =
        EntityModel.of(user,
            linkTo<UserController> { getCurrentUser(user) }.withSelfRel(),
            linkTo<UserController> { getCurrentUser(user) }.withRel("user")
        )

}