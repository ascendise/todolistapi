package ch.ascendise.todolistapi.user

import ch.ascendise.todolistapi.checklist.Checklist
import ch.ascendise.todolistapi.checklist.ChecklistService
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskService
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

internal class UserServiceTest {

    private lateinit var service: UserService

    private val userRepository = mockk<UserRepository>()
    private val taskService = mockk<TaskService>()
    private val checklistService = mockk<ChecklistService>()

    @BeforeEach
    fun setUp() {
        service = UserService(userRepository, checklistService, taskService)
    }

    @Test
    fun `should return user with specified subject`() {
        val subject = "auth|12345"
        val jwt = mockk<Jwt>()
        every { jwt.subject } returns subject
        val expectedUser = User(id = 101, subject = subject, username = "John Doe")
        every { userRepository.findBySubject(subject) } returns expectedUser
        val actualUser = service.getUser(jwt)
        assertEquals(expectedUser, actualUser)
        verifySequence {
            jwt.subject
            userRepository.findBySubject(subject)
        }
    }

    @Test
    fun `should delete user and all associated ressources`() {
        val user = User(id = 101, subject = "auth|12345", username = "John Doe")
        val checklists = listOf(
            Checklist(id = 301, name = "Checklist 1", user = user),
            Checklist(id = 302, name = "Checklist 2", user = user)
        )
        every { checklistService.getChecklists(user.id) } returns checklists
        val tasks = setOf(
            Task(id = 201, name = "Task 1", user = user),
            Task(id = 202, name = "Task 2", user = user)
        )
        every { taskService.getAll(user.id) } returns tasks
        every { checklistService.delete(any(), user.id) } returns Unit
        every { taskService.delete(user.id, any())} returns Unit
        every { userRepository.delete(user) } returns Unit
        service.delete(user)
        verifyAll {
            checklistService.getChecklists(user.id)
            checklistService.delete(301, user.id)
            checklistService.delete(302, user.id)
            taskService.getAll(user.id)
            taskService.delete(user.id, 201)
            taskService.delete(user.id, 202)
            userRepository.delete(user)
        }
    }

}