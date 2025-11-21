package squad_api.squad_api.application.dto

import java.time.LocalDate

data class AllocationCreateRequest(
    val startedAt: LocalDate,
    val allocatedHours: Int,
    val personId: Long,
    val position: String,
)