package ch.ascendise.todolistapi

data class ApiError (
    val statusCode: Long,
    val name: String,
    val description: String
        ){
}