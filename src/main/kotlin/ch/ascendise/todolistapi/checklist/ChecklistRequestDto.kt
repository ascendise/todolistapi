package ch.ascendise.todolistapi.checklist

import ch.ascendise.todolistapi.user.User

class ChecklistRequestDto(
    val name: String
) {

    fun toChecklist(user: User): Checklist =
        Checklist(name = this.name, user = user)
}