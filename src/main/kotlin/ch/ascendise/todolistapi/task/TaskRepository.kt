package ch.ascendise.todolistapi.task

import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long>
{
    fun findAllByUserId(id: Long): List<Task>
}