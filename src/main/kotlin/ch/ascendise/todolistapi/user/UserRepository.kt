package ch.ascendise.todolistapi.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User,Long>