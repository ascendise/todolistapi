package ch.ascendise.todolistapi.task

class StartDateBeforeTodayTaskException(
    override val message: String = "The date in field 'startDate' must not be before today"
):
    InvalidTaskException() {
}