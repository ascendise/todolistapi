package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class ChecklistServiceTest {

    private val checklistRepository = mockk<ChecklistRepository>()
    private val user = User(id = 100, username = "user", subject = "auth-oauth2|123451234512345")
    private lateinit var service: ChecklistService

    @BeforeEach
    fun setUp() {
        service = ChecklistService(checklistRepository)
    }

    @Test
    fun `should return all checklists`(){
        val expectedChecklists = listOf(Checklist(name = "New Checklist1", user = user), Checklist(name = "New Checklist2", user = user))
        every { checklistRepository.findAllByUserId(user.id) } returns expectedChecklists
        val checklists = service.getChecklists(user.id)
        verify { checklistRepository.findAllByUserId(user.id) }
        assertEquals(expectedChecklists, checklists)
    }

    @Test
    fun `should return checklists with empty list`() {
        every { checklistRepository.findAllByUserId(user.id) } returns emptyList()
        val checklists = service.getChecklists(user.id)
        verify { checklistRepository.findAllByUserId(user.id) }
        assertEquals(emptyList<Checklist>(), checklists)
    }

    @Test
    fun `should return checklist with specified id`() {
        val expected = Checklist(id = 101, name = "New Checklist1", user = user)
        every { checklistRepository.findByIdAndUserId(expected.id, user.id) } returns Optional.of(expected)
        val checklist = service.getChecklist(expected.id, user.id)
        verify { checklistRepository.findByIdAndUserId(expected.id, user.id) }
        assertEquals(expected, checklist)
    }

    @Test
    fun `should throw ChecklistNotFoundException if no checklist with specified id exists`() {
        val id = 101L
        every { checklistRepository.findByIdAndUserId(id, user.id) } returns Optional.empty()
        assertThrows<ChecklistNotFoundException> { service.getChecklist(id, user.id) }
        verify { checklistRepository.findByIdAndUserId(id, user.id) }
    }

    @Test
    fun `should create new checklist`() {
        val newChecklist = Checklist(name = "New Checklist2", user = user)
        every { checklistRepository.save(newChecklist) } returns newChecklist
        val returnedChecklist = service.create(newChecklist)
        verify { checklistRepository.save(newChecklist) }
        assertEquals(newChecklist, returnedChecklist)
    }

    @Test
    fun `should update existing checklist`() {
        val checklist = Checklist(id = 101, name = "Shopping List", user = user)
        val oldChecklist = Checklist(id = 101, name = "New Checklist", user = user)
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(oldChecklist)
        every { checklistRepository.save(oldChecklist) } returns checklist
        val newChecklist = service.update(checklist)
        verify { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
        verify { checklistRepository.save(oldChecklist) }
        assertEquals(checklist, newChecklist)
    }

    @Test
    fun `should throw ChecklistNotFoundException if to be updated checklist does not exist`() {
        val checklist = Checklist(id = -1L, name = "Shopping List", user = user)
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.empty()
        assertThrows<ChecklistNotFoundException> { service.update(checklist) }
        verify { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
    }

    @Test
    fun `should delete checklist`() {
        val checklistId = 101L
        justRun { checklistRepository.deleteByIdAndUserId(checklistId, user.id) }
        service.delete(checklistId, user.id)
        verify { checklistRepository.deleteByIdAndUserId(checklistId, user.id) }
    }
}


























