package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class Task (
    @Id @GeneratedValue var id: Long = 0,
    var name: String,
    var description: String = "",
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null,
    @ManyToOne var user: User
        ){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + user.hashCode()
        return result
    }
}