package squad_api.squad_api.domain.utils

import squad_api.squad_api.application.dto.AllocationResponse
import squad_api.squad_api.application.dto.TeamShortDTO
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.service.PopsSrvEmployeeClient

class AllocationNormalizer(
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient
) {
    fun normalize(allocation: Allocation, token: String): AllocationResponse {
        val employee = popsSrvEmployeeClient.findEmployeeById(allocation.personId, token)
        return AllocationResponse(
            id = allocation.id!!,
            startedAt = allocation.startedAt,
            allocatedHours = allocation.allocatedHours,
            team = TeamShortDTO(
                id = allocation.team.id!!,
                name = allocation.team.name
            ),
            employee = employee,
            personId = if (employee == null) allocation.personId else null,
            position = allocation.position
        )
    }
}