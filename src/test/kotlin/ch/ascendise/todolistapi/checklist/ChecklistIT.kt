package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.ApiError
import ch.ascendise.todolistapi.task.Task
import ch.ascendise.todolistapi.task.TaskRepository
import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import javax.transaction.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
internal class ChecklistIT {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var checklistRepository: ChecklistRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    private lateinit var jackson: ObjectMapper
    private lateinit var jwt: Jwt

    private val user = User(subject = "auth-oauth2|123451234512345")
    private val otherUser = User(subject = "auth-oauth2|543215432154321")
    private val checklists = listOf(
        Checklist(name = "Checklist1", user = user, tasks = mutableListOf(
            Task(name = "Task1-1", user = user),
            Task(name = "Task1-2", user = user)
        )),
        Checklist(name = "Checklist2", user = user, tasks = mutableListOf())
    )

    @BeforeEach
    fun setUp() {
        userRepository.saveAll(setOf(user, otherUser))
        taskRepository.saveAll(checklists[0].tasks)
        checklistRepository.saveAll(checklists)
        jackson = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        jwt = getJwt()
    }

    fun getJwt(): Jwt {
        val jwt = mockk<Jwt>()
        every { jwt.subject }.returns(user.subject)
        every { jwt.hasClaim(any())}.answers { callOriginal() }
        every { jwt.claims}.returns(mapOf( "sub" to user.subject))
        return jwt
    }

    @AfterEach
    fun tearDown() {
        checklistRepository.deleteAll()
        taskRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `GET checklists should return all checklists of user`() {
        //Arrange
        //Act
        val response = mockMvc.perform(
            get("/checklists").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        //Assert
        val json = jackson.readTree(response.response.contentAsString)
        val checklistsJson = json.at("/_embedded/checklists").toString()
        val checklists: List<ChecklistResponseDto> = jackson.readValue(checklistsJson)
        val expectedChecklists = this.checklists.stream().map { it.toChecklistResponseDto() }.toList()
        assertEquals(expectedChecklists, checklists)
    }

    @Test
    fun `GET checklists should include links to possible actions when GETting all checklists`() {
        //Arrange
        //Act
        mockMvc.perform(
            get("/checklists").with(jwt().jwt(jwt))
        )
        //Assert
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.self.href", `is`("http://localhost/checklists/${checklists[0].id}")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.checklists.href", `is`("http://localhost/checklists")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0]._links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0].tasks[0]._links.self.href", `is`("http://localhost/tasks/${checklists[0].tasks[0].id}")))
            .andExpect(jsonPath("_embedded.checklists[0].tasks[0]._links.tasks.href", `is`("http://localhost/tasks")))
            .andExpect(jsonPath("_embedded.checklists[0].tasks[0]._links.removeTask.href",
                `is`("http://localhost/checklists/${checklists[0].id}/tasks/${checklists[0].tasks[0].id}"))
            )
    }

    @Test
    fun `GET checklists{id} should return specific checklist for user`() {
        //Arrange
        val expectedChecklist = checklists[0]
        //Act
        val response = mockMvc.perform(
            get("/checklists/${expectedChecklist.id}").with(jwt().jwt(jwt))
        )
            .andExpect(status().isOk)
            .andReturn()
        //Assert
        val checklist: ChecklistResponseDto = jackson.readValue(response.response.contentAsString)
        assertEquals(expectedChecklist.toChecklistResponseDto(), checklist)
    }

    @Test
    fun `GET checklists{id} should return links to possible operations when GETting single checklist`() {
        //Arrange
        val checklist = checklists[0]
        //Act
        mockMvc.perform(
            get("/checklists/${checklist.id}").with(jwt().jwt(jwt))
        )
        //Assert
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/checklists/${checklist.id}")))
            .andExpect(jsonPath("_links.checklists.href", `is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("tasks[0]._links.self.href", `is`("http://localhost/tasks/${checklist.tasks[0].id}")))
            .andExpect(jsonPath("tasks[0]._links.tasks.href", `is`("http://localhost/tasks")))
            .andExpect(jsonPath("tasks[0]._links.removeTask.href",
                `is`("http://localhost/checklists/${checklists[0].id}/tasks/${checklists[0].tasks[0].id}"))
            )
    }

    @Test
    fun `GET checklist should include complete operation if checklist tasks are all done`() {
        //Arrange
        val checklist = checklists[0]
        checklist.tasks.forEach { it.isDone = true }
        //Act
        mockMvc.perform(
            get("/checklists/${checklist.id}").with(jwt().jwt(jwt))
        )
            //Assert
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.complete.href", `is`("http://localhost/checklists/${checklist.id}/complete")))
    }

    @Test
    fun `POST checklists should create new checklist on POST and return it`() {
        //Arrange
        val newChecklist = ChecklistRequestDto(name = "My cool new Checklist")
        val requestJson = jackson.writeValueAsString(newChecklist)
        //Act
        val response = mockMvc.perform(
            post("/checklists")
                .with(jwt().jwt(jwt))
                .content(requestJson)
                .contentType("application/json")
        )
            .andExpect(status().isCreated)
            .andReturn()
        //Assert
        val createdChecklistInDb = checklistRepository.findAllByUserId(user.id)
            .filter { it.name == "My cool new Checklist" }[0].toChecklistResponseDto()
        val createdChecklistResponse: ChecklistResponseDto = jackson.readValue(response.response.contentAsString)
        assertEquals(createdChecklistInDb, createdChecklistResponse)
    }

    @Test
    fun `POSt checklists should return possible operations on entity returned by POST request`() {
        //Arrange
        val newChecklist = ChecklistRequestDto(name = "My cool new Checklist")
        val requestJson = jackson.writeValueAsString(newChecklist)
        val expectedId = checklistRepository.findAll().last().id + 1
        //Act
        mockMvc.perform(
            post("/checklists")
                .with(jwt().jwt(jwt))
                .content(requestJson)
                .contentType("application/json")
        )
        //Assert
            .andExpect(status().isCreated)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/checklists/${expectedId}")))
            .andExpect(jsonPath("_links.checklists.href", `is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
    }

    @Test
    fun `PUT checklists{id} should update name of checklist`() {
        //Arrange
        val oldChecklist = checklists[0]
        val updateRequest = jackson.writeValueAsString(ChecklistRequestDto(name = "New checklist name"))
        //Act
        val response = mockMvc.perform(
            put("/checklists/${oldChecklist.id}")
                .with(jwt().jwt(jwt))
                .content(updateRequest)
                .contentType("application/json")
        )
            .andExpect(status().isOk)
            .andReturn()
        //Assert
        val updatedChecklist: ChecklistResponseDto = jackson.readValue(response.response.contentAsString)
        val updatedChecklistInDb = checklistRepository.findByIdAndUserId(oldChecklist.id, user.id).get().toChecklistResponseDto()
        assertEquals(updatedChecklistInDb, updatedChecklist)
    }

    @Test
    fun `PUT checklists{id} should return 404 when trying to PUT checklist that does not exist`() {
        //Arrange
        val updateRequest = jackson.writeValueAsString(ChecklistRequestDto(name = "New checklist name"))
        //Act
        mockMvc.perform(
            put("/checklists/999999")
                .with(jwt().jwt(jwt))
                .content(updateRequest)
                .contentType("application/json")
        )
        //Assert
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PUT checklists{id} should return HATEOAS entity for updated checklist`() {
        //Arrange
        val oldChecklist = checklists[0]
        val updateRequest = jackson.writeValueAsString(ChecklistRequestDto(name = "New checklist name"))
        //Act
        mockMvc.perform(
            put("/checklists/${oldChecklist.id}")
                .with(jwt().jwt(jwt))
                .content(updateRequest)
                .contentType("application/json")
        )
        //Assert
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("_links.self.href", `is`("http://localhost/checklists/${oldChecklist.id}")))
            .andExpect(jsonPath("_links.checklists.href", `is`("http://localhost/checklists")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
            .andExpect(jsonPath("_links.relations.href", `is`("http://localhost/checklists/tasks")))
    }

    @Test
    fun `DELETE checklists{id} should delete checklist on DELETE request`() {
        //Arrange
        val checklist = checklists[0]
        //Act
        mockMvc.perform(
            delete("/checklists/${checklist.id}")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isNoContent)
        //Assert
        val result = checklistRepository.findByIdAndUserId(checklist.id, user.id).orElseGet { null }
        assertNull(result)
    }


    @Test
    fun `DELETE checklists{id} should return NO Content on DELETE if checklist does not exist`() {
        //Arrange
        //Act
        mockMvc.perform(
            delete("/checklists/99999")
                .with(jwt().jwt(jwt))
        )
        //Assert
            .andExpect(status().isNoContent)
    }

    @Test
    fun `complete checklists{id} should delete checklist and all tasks inside it`() {
        //Arrange
        val checklist = checklists[0]
        checklist.tasks.forEach { it.isDone = true }
        val checklistId = checklist.id
        val taskId1 = checklist.tasks[0].id
        val taskId2 = checklist.tasks[1].id
        //Act
        mockMvc.perform(
            post("/checklists/${checklist.id}/complete")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isNoContent)
        //Assert
        val checklistDeleted = checklistRepository.findByIdAndUserId(checklistId, user.id).orElseGet { null } == null
        val task1Deleted = taskRepository.findByIdAndUserId(taskId1, user.id).orElseGet { null } == null
        val task2Deleted = taskRepository.findByIdAndUserId(taskId2, user.id).orElseGet { null } == null
        assertTrue(checklistDeleted)
        assertTrue(task1Deleted)
        assertTrue(task2Deleted)
    }

    @Test
    fun `complete checklists{id} should return 404 if checklist does not exist`() {
        mockMvc.perform(
            post("/checklists/999999/complete")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `complete checklists{id} should return 400 if not all tasks in checklist are done`() {
        //Arrange
        val checklist = checklists[0]
        checklist.tasks.forEach { it.isDone = false }
        //Act
        val response = mockMvc.perform(
            post("/checklists/${checklist.id}/complete")
                .with(jwt().jwt(jwt))
        )
            .andExpect(status().isBadRequest)
            .andReturn()
        //Assert
        val apiErrorResponse: ApiError = jackson.readValue(response.response.contentAsString)
        val expectedApiError = ApiError(
            statusCode = 400,
            name = "Bad Request",
            description = "Failed to complete checklist. Not all tasks are marked done. " +
                    "Did you mean to use query parameter 'force' or 'partial'?"
        )
        assertEquals(apiErrorResponse, expectedApiError)
    }

}