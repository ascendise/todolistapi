package ch.ascendise.todolistapi.user

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.security.core.annotation.AuthenticationPrincipal

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal(expression = "@userService.getUser(#this)")
@Parameter(hidden = true)
annotation class CurrentUser
