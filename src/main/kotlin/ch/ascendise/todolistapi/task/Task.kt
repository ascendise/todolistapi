package ch.ascendise.todolistapi.task

import java.util.Date
import javax.persistence.GeneratedValue
import javax.persistence.Id

class Task (
    @Id @GeneratedValue var id: Long,
    var name: String,
    var description: String,
    var startDate: Date?,
    var endDate: Date?
        ){
}