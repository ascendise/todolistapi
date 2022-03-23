package ch.ascendise.todolistapi.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User(
    @Id @GeneratedValue var id: Long,
    var email: String,
    var username: String)
