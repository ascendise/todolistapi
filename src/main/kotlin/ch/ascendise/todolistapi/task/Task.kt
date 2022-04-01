package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Task (
    @Id @GeneratedValue var id: Long = 0,
    var name: String,
    var description: String,
    var startDate: Date?,
    var endDate: Date?
        ){
}