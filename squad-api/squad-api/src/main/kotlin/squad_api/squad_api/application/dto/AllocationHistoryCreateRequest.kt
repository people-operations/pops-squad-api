package squad_api.squad_api.application.dto

import java.time.LocalDate

data class AllocationHistoryCreateRequest(
    val startedAt: LocalDate,
    val endedAt: LocalDate,
    val allocatedHours: Int,
    val personId: Long,
    val position: String,
    val team : Long
)