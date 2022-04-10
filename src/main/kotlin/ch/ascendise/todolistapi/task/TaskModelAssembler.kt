package ch.ascendise.todolistapi.task

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class TaskModelAssembler(
) : RepresentationModelAssembler<Task, EntityModel<Task>> {

    override fun toModel(task: Task): EntityModel<Task> =
        EntityModel.of(task,
            linkTo<TaskController> {getTask(task.user, task.id)}.withSelfRel(),
            linkTo<TaskController> {getTasks(task.user)}.withRel("tasks")
        )

}