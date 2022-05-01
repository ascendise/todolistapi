package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.task.Task
import org.springframework.hateoas.RepresentationModel
import javax.persistence.*

@Entity
class Checklist(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var name: String,
    @ManyToMany(targetEntity = Task::class) var tasks: MutableList<Task> = mutableListOf(),
    @ManyToOne var user: User
): RepresentationModel<Checklist>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Checklist

        if (id != other.id) return false
        if (name != other.name) return false
        if (tasks != other.tasks) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + tasks.hashCode()
        result = 31 * result + user.hashCode()
        return result
    }
}