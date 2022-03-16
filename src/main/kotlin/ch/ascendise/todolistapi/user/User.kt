package ch.ascendise.todolistapi.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class User(
    @Id @GeneratedValue val id: Long,
    val email: String,
    val username: String)
