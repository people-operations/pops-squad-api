package squad_api.squad_api.application.dto

import java.time.LocalDate

data class AllocationResponse(
    val id: Long,
    val startedAt: LocalDate,
    val allocatedHours: Int,
    val team: TeamShortDTO,
    val personId: Long?,
    val employee: EmployeeDTO?,
    val position: String
)

data class TeamShortDTO(
    val id: Long,
    val name: String
)