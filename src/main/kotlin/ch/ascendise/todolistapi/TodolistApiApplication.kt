package ch.ascendise.todolistapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files
import java.nio.file.Paths

@SpringBootApplication
class TodolistApiApplication

fun main(args: Array<String>) {
	createPropertiesFileIfNotExists()
	runApplication<TodolistApiApplication>(*args)
}

fun createPropertiesFileIfNotExists() {
	val newFile = Paths.get("./application.yml")
	if(!Files.exists(newFile)) {
		val propertiesFileStream = TodolistApiApplication::class.java.getResourceAsStream("/templates/application_template.yml")
			?: throw java.lang.RuntimeException("Template for application properties could not be loaded!");
		Files.write(newFile, propertiesFileStream.readAllBytes())
	}
}