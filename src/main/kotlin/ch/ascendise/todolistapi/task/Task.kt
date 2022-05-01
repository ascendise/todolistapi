package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.RepresentationModel
import java.time.LocalDate
import javax.persistence.*

@Entity
class Task (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var name: String,
    var description: String = "",
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null,
    var isDone: Boolean = false,
    @ManyToOne var user: User
        ): RepresentationModel<Task>(){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (isDone != other.isDone) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + (isDone?.hashCode() ?: 0)
        result = 31 * result + user.hashCode()
        return result
    }
}