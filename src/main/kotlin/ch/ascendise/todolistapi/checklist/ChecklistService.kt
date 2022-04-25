package ch.ascendise.todolistapi.checklist

import org.springframework.stereotype.Service

@Service
class ChecklistService(
    val checklistRepository: ChecklistRepository
) {

    fun getChecklists(userId: Long) : List<Checklist> =
        checklistRepository.findAllByUserId(userId)
}