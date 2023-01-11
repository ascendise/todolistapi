package ch.ascendise.todolistapi.checklist

class ChecklistIncompleteException() : Exception("Checklist cannot be completed as it includes undone tasks") {
}