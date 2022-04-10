package ch.ascendise.todolistapi.task

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TaskRepository : JpaRepository<Task, Long>
{
    fun findAllByUserId(id: Long): List<Task>
    fun findByIdAndUserId(id: Long, userId: Long): Optional<Task>
    fun deleteByIdAndUserId(id: Long, userId: Long)
}