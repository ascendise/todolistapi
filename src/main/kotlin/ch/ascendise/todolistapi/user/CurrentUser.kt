package ch.ascendise.todolistapi.user

import org.springframework.security.core.annotation.AuthenticationPrincipal

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal(expression = "@userService.getUser(#this)")
annotation class CurrentUser()
