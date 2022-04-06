package ch.ascendise.todolistapi.task

class InvalidDateRangeTaskException(
    override val message: String = "The date in field 'endDate' must not be before 'startDate'"
): InvalidTaskException() {
}