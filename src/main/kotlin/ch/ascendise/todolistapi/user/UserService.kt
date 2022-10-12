package ch.ascendise.todolistapi.user

import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.TaskService
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
    val checklistService: ChecklistService,
    val taskService: TaskService
) {

    fun getUser(jwt: Jwt) =
        userRepository.findBySubject(jwt.subject)

    fun delete(user: User) {
        checklistService.getChecklists(user.id).asSequence()
            .forEach { checklistService.delete(it.id, user.id) }
        taskService.getAll(user.id).asSequence()
            .forEach { taskService.delete(user.id, it.id)}
        userRepository.delete(user)
    }
}