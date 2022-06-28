package ch.ascendise.todolistapi

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.RequestDispatcher.ERROR_STATUS_CODE
import javax.servlet.http.HttpServletRequest

@RestController
class ErrorController: org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): ResponseEntity<Any> {
        val statusCode = request.getAttribute(ERROR_STATUS_CODE) as Int
        return ResponseEntity.status(statusCode).build()
    }
}