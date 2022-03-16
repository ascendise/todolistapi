package ch.ascendise.todolistapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodolistApiApplication

fun main(args: Array<String>) {
	runApplication<TodolistApiApplication>(*args)
}
