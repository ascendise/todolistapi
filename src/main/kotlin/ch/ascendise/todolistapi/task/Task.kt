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
    var description: String,
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null,
    @ManyToOne(cascade = [CascadeType.ALL]) var user: User
        ){
}