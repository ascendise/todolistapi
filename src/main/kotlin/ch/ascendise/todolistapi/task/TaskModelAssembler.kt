package ch.ascendise.todolistapi.task

import ch.ascendise.todolistapi.user.User
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.stereotype.Component

@Component
class TaskModelAssembler() : RepresentationModelAssembler<Task, TaskResponseDto> {

    override fun toModel(task: Task): TaskResponseDto {
        val taskDto = task.toTaskResponseDto()
        taskDto.add(linkTo<TaskController> {getTask(task.user, task.id)}.withSelfRel())
        taskDto.add(linkTo<TaskController> {getTasks(task.user)}.withRel("tasks"))
        return taskDto
    }

    fun toModel(taskDto: TaskResponseDto, user: User): TaskResponseDto {
        val task = taskDto.toTask(user)
        val taskDtoWithLinks = toModel(task)
        taskDto.add(taskDtoWithLinks.links)
        return taskDto
    }


}