package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import kotlin.math.exp

@SpringBootTest
class ChecklistServiceTest {

    @MockkBean
    private lateinit var checklistRepository: ChecklistRepository

    @Autowired
    private lateinit var checklistService: ChecklistService

    private val user = User(id = 1, username = "User", email = "email@domain.com")

    @Test
    fun `Fetch all checklists`(){
        val expectedChecklists = listOf(Checklist(name = "New Checklist1", user = user), Checklist(name = "New Checklist2", user = user))
        every { checklistRepository.findAllByUserId(user.id) } returns expectedChecklists
        val checklists = checklistService.getChecklists(user.id)
        verify { checklistRepository.findAllByUserId(user.id) }
        assertEquals(expectedChecklists, checklists)
    }

    @Test
    fun `Fetching checklists may return empty list`() {
        every { checklistRepository.findAllByUserId(user.id) } returns emptyList()
        val checklists = checklistService.getChecklists(user.id)
        verify { checklistRepository.findAllByUserId(user.id) }
        assertEquals(emptyList<Checklist>(), checklists)
    }

    @Test
    fun `Fetching single checklist`() {
        val expected = Checklist(id = 1, name = "New Checklist1", user = user)
        every { checklistRepository.findByIdAndUserId(expected.id, user.id) } returns Optional.of(expected)
        val checklist = checklistService.getChecklist(expected.id, user.id)
        verify { checklistRepository.findByIdAndUserId(expected.id, user.id) }
        assertEquals(expected, checklist)
    }

    @Test
    fun `Fetching nonexistent resource throws ChecklistNotFoundException`() {
        val id = 1L
        every { checklistRepository.findByIdAndUserId(id, user.id) } returns Optional.empty()
        assertThrows<ChecklistNotFoundException> { checklistService.getChecklist(id, user.id) }
        verify { checklistRepository.findByIdAndUserId(id, user.id) }
    }

    @Test
    fun `Create new checklist`() {
        val newChecklist = Checklist(name = "New Checklist2", user = user)
        every { checklistRepository.save(newChecklist) } returns newChecklist
        val returnedChecklist = checklistService.create(newChecklist)
        verify { checklistRepository.save(newChecklist) }
        assertEquals(newChecklist, returnedChecklist)
    }

    @Test
    fun `Update existing checklist`() {
        val checklist = Checklist(id = 1, name = "Shopping List", user = user)
        val oldChecklist = Checklist(id = 1, name = "New Checklist", user = user)
        every { checklistRepository.findByIdAndUserId(checklist.id, user.id) } returns Optional.of(oldChecklist)
        every { checklistRepository.save(oldChecklist) } returns checklist
        val newChecklist = checklistService.update(checklist, user.id)
        verify { checklistRepository.findByIdAndUserId(checklist.id, user.id) }
        verify { checklistRepository.save(oldChecklist) }
        assertEquals(checklist, newChecklist)
    }
}


























