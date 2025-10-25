package squad_api.squad_api.domain.service

import org.springframework.stereotype.Service
import squad_api.squad_api.application.dto.AllocationCreateRequest
import squad_api.squad_api.application.dto.AllocationResponse
import squad_api.squad_api.application.dto.TeamShortDTO
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.repository.AllocationRepository
import squad_api.squad_api.infraestructure.utilities.CrudService

@Service
class AllocationService(
    private val allocationRepository: AllocationRepository,
    private val teamService: TeamService,
    private val allocationHistoryService: AllocationHistoryService,
    private val allocationDeleteService: AllocationDeleteService
) : CrudService<Allocation, Long>() {

    override val repository = allocationRepository

    fun findAllByTeamId(teamId: Long): List<AllocationResponse> {
        return repository.findAllByTeamId(teamId)
            .map { toAllocationResponseDTO(it) }
    }

    fun toAllocationResponseDTO(allocation: Allocation): AllocationResponse {
        return AllocationResponse(
            id = allocation.id!!,
            startedAt = allocation.startedAt,
            allocatedHours = allocation.allocatedHours,
            team = TeamShortDTO(
                id = allocation.team.id!!,
                name = allocation.team.name
            ),
            personId = allocation.personId,
            position = allocation.position
        )
    }

    fun replaceAllocations(teamId: Long, request: AllocationCreateRequest): Allocation {
        val team = teamService.findById(teamId)
        val entity = Allocation(
            startedAt = request.startedAt,
            team = team,
            allocatedHours = request.allocatedHours,
            personId = request.personId,
            position = request.position
        )
        val currentAllocations = findAllByTeamId(teamId)
        allocationHistoryService.saveCurrentAllocationsAsHistory(currentAllocations)
        allocationDeleteService.deleteAllByTeamId(teamId)
        return save(entity)
    }
}