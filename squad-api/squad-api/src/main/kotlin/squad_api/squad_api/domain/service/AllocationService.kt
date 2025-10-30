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
    private val allocationDeleteService: AllocationDeleteService,
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient
) : CrudService<Allocation, Long>() {

    override val repository = allocationRepository

    fun findAllByTeamId(teamId: Long, token: String): List<Allocation> {
        val allocations = repository.findAllByTeamId(teamId)
        if (allocations.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma alocação encontrada para o time com ID: $teamId")
        }
        return allocations
    }

    fun replaceAllocations(teamId: Long, request: List<AllocationCreateRequest>, token: String): List<Allocation> {
        val team = teamService.findById(teamId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum time encontrado ID: $teamId")

        val currentAllocations = findAllByTeamId(teamId, token)

        allocationHistoryService.saveCurrentAllocationsAsHistory(currentAllocations)
        allocationDeleteService.deleteAllByTeamId(teamId)

        return request.map { allocationReq ->
            val entity = Allocation(
                startedAt = allocationReq.startedAt,
                team = team,
                allocatedHours = allocationReq.allocatedHours,
                personId = allocationReq.personId,
                position = allocationReq.position
            )
            save(entity)
        }
    }

    fun findAllByPersonId(personId: Long, token: String): List<Allocation> {
        val allocations = allocationRepository.findAllByPersonId(personId)
        if (allocations.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma alocação encontrada para personId: $personId")
        }
        return allocations
    }
}