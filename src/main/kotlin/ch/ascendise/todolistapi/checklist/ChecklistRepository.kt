package ch.ascendise.todolistapi.checklist

import org.springframework.data.jpa.repository.JpaRepository

interface ChecklistRepository : JpaRepository<Checklist, Long> {
    fun findAllByUserId(userId: Long) : List<Checklist>
    fun findByIdAndUserId(id: Long, userId: Long) : Checklist
}