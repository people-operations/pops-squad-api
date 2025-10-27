package squad_api.squad_api.domain.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
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
    , private val popsSrvEmployeeClient: PopsSrvEmployeeClient
) : CrudService<Allocation, Long>() {

    override val repository = allocationRepository

    fun findAllByTeamId(teamId: Long, token: String): List<AllocationResponse> {
        val allocations = repository.findAllByTeamId(teamId)
        if (allocations.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma alocação encontrada para o time com ID: $teamId")
        }
        return allocations.map { toAllocationResponseDTO(it, token) }
    }

    fun toAllocationResponseDTO(allocation: Allocation, token: String): AllocationResponse {
        println("Trying to fetch employee for personId: ${allocation.personId}")
        val employee = popsSrvEmployeeClient.findEmployeeById(allocation.personId, token)
            ?: throw IllegalStateException("Employee não encontrado para personId: ${allocation.personId}")
        println("Employee fetched: $employee")
        return AllocationResponse(
            id = allocation.id!!,
            startedAt = allocation.startedAt,
            allocatedHours = allocation.allocatedHours,
            team = TeamShortDTO(
                id = allocation.team.id!!,
                name = allocation.team.name
            ),
            employee = employee,
            personId = allocation.personId,
            position = allocation.position
        )
    }

    fun replaceAllocations(teamId: Long, request: AllocationCreateRequest, token: String): Allocation {
        val team = teamService.findById(teamId)

        val entity = Allocation(
            startedAt = request.startedAt,
            team = team,
            allocatedHours = request.allocatedHours,
            personId = request.personId,
            position = request.position
        )
        val currentAllocations = findAllByTeamId(teamId, token)
        allocationHistoryService.saveCurrentAllocationsAsHistory(currentAllocations)
        allocationDeleteService.deleteAllByTeamId(teamId)
        return save(entity)
    }

    fun findAllByPersonId(personId: Long, token: String): List<AllocationResponse> {
        val employee = popsSrvEmployeeClient.findEmployeeById(personId, token)
            ?: throw IllegalStateException("Employee não encontrado para personId: $personId")
        return allocationRepository.findAllByPersonId(personId)
            .map { allocation ->
                AllocationResponse(
                    id = allocation.id!!,
                    startedAt = allocation.startedAt,
                    allocatedHours = allocation.allocatedHours,
                    team = TeamShortDTO(
                        id = allocation.team.id!!,
                        name = allocation.team.name
                    ),
                    employee = employee,
                    personId = allocation.personId,
                    position = allocation.position
                )
            }
    }
}