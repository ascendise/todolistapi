package ch.ascendise.todolistapi.task

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

interface TaskRepository : JpaRepository<Task, Long>
{
    fun findAllByUserId(id: Long): List<Task>
    fun findByIdAndUserId(id: Long, userId: Long): Optional<Task>
    @Transactional fun deleteByIdAndUserId(id: Long, userId: Long)
}