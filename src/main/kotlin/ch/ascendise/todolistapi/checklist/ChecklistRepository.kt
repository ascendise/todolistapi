package ch.ascendise.todolistapi.checklist

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.transaction.Transactional

@Transactional
interface ChecklistRepository : JpaRepository<Checklist, Long> {
    fun findAllByUserId(userId: Long) : List<Checklist>
    fun findByIdAndUserId(id: Long, userId: Long) : Optional<Checklist>
    fun deleteByIdAndUserId(id: Long, userId: Long)

}