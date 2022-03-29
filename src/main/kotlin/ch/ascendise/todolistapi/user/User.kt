package ch.ascendise.todolistapi.user

import ch.ascendise.todolistapi.task.Task
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class User(
    @Id @GeneratedValue var id: Long,
    @Column(unique = true) var email: String,
    @Column(unique = true) var username: String,
    @OneToMany var tasks: Set<Task> = emptySet()
    )
