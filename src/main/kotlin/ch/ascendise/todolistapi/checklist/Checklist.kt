package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User
import javax.persistence.*

@Entity
class Checklist(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var name: String,
    @ManyToOne var user: User
) {
}