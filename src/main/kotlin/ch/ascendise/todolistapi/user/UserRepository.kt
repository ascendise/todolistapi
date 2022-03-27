package ch.ascendise.todolistapi.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User,Long> {
    fun findByEmail(email: String): User?
    fun deleteByEmail(email: String)
}