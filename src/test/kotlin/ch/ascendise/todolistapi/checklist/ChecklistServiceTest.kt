package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

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
    fun `Fetching checklists can return empty list`() {
        every { checklistRepository.findAllByUserId(user.id) } returns emptyList()
        val checklists = checklistService.getChecklists(user.id)
        verify { checklistRepository.findAllByUserId(user.id) }
        assertEquals(emptyList<Checklist>(), checklists)
    }
}