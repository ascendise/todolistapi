package ch.ascendise.todolistapi.user

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
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