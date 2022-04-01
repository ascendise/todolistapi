package ch.ascendise.todolistapi.task

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Task (
    @Id @GeneratedValue var id: Long = 0,
    var name: String,
    var description: String,
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate? = null
        ){
}