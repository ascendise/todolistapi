package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import ch.ascendise.todolistapi.user.UserModelAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class TaskModelAssembler(
    val userModelAssembler: UserModelAssembler
) : RepresentationModelAssembler<Task, Task> {

    override fun toModel(task: Task): Task {
        task.add(linkTo<TaskController> {getTask(task.user, task.id)}.withSelfRel())
        task.add(linkTo<TaskController> {getTasks(task.user)}.withRel("tasks"))
        task.user = userModelAssembler.toModel(User(task.user))
        return task
    }


}