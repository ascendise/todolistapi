package ch.ascendise.todolistapi.user

import org.springframework.hateoas.RepresentationModel
import javax.persistence.*

@Entity
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @Column(unique = true) var subject: String
    ): RepresentationModel<User>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (subject != other.subject) return false
        if (links != other.links) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + subject.hashCode()
        return result
    }
}
