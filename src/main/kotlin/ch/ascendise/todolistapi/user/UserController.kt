package ch.ascendise.todolistapi.user

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SecurityRequirement(name = "bearer-key")
class UserController(
    val userService: UserService,
    val userModelAssembler: UserModelAssembler
) {

    @GetMapping("/user")
    fun getCurrentUser(@CurrentUser user: User) : User =
        userModelAssembler.toModel(user)

    @DeleteMapping("/user")
    fun deleteCurrentUser(@CurrentUser user: User) : ResponseEntity<User> {
        userService.delete(user)
        return ResponseEntity.noContent().build()
    }
}