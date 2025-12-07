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
        // Retornar lista vazia ao invés de lançar exceção quando não há allocations
        // Isso permite que teams sem allocations sejam tratados normalmente
        return allocations
    }

    fun replaceAllocations(teamId: Long, request: List<AllocationCreateRequest>, token: String): List<Allocation> {
        val team = teamService.findById(teamId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum time encontrado ID: $teamId")

        // Buscar allocations atuais (pode retornar lista vazia se não houver)
        val currentAllocations = findAllByTeamId(teamId, token)

        // Salvar histórico apenas se houver allocations atuais
        if (currentAllocations.isNotEmpty()) {
            allocationHistoryService.saveCurrentAllocationsAsHistory(currentAllocations)
        }
        
        // Deletar allocations atuais (mesmo que seja vazio, é seguro chamar)
        allocationDeleteService.deleteAllByTeamId(teamId)

        // Criar novas allocations
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
        // Retornar lista vazia ao invés de lançar exceção quando não há allocations
        return allocations
    }
}