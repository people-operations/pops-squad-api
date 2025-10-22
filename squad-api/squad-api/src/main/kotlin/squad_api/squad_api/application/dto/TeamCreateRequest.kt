package squad_api.squad_api.application.dto

data class TeamCreateRequest(
    val name: String,
    val description: String?,
    val sprintDuration: Int,
    val approverId: Long?,
    val projectId: Long
)