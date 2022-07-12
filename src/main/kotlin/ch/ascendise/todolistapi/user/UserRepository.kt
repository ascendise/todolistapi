package ch.ascendise.todolistapi.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User,Long> {
    fun findBySubject(subject: String): User
    fun existsBySubject(subject: String): Boolean
}