package ch.ascendise.todolistapi.user

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User(
    @Id @GeneratedValue var id: Long,
    @Column(unique = true) var email: String,
    @Column(unique = true) var username: String)
