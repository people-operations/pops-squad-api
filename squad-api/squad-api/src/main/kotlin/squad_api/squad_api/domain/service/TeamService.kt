package squad_api.squad_api.domain.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.ApproverDTO
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.TeamRepository
import squad_api.squad_api.domain.repository.AllocationRepository
import squad_api.squad_api.infraestructure.utilities.CrudService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient,
    private val allocationRepository: AllocationRepository

) : CrudService<Team, Long>() {

    override val repository = teamRepository


    fun findTeamById(teamId: Long, token: String):TeamResponseDTO {
        val team = repository.findById(teamId)
        if (team.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma alocação encontrada para o time com ID: $teamId")
        }
        return toTeamResponseDTO(team.get(), token)
    }

    fun toTeamResponseDTO(team: Team, token: String): TeamResponseDTO {
        val approver = team.approverId?.let { popsSrvEmployeeClient.findEmployeeById(it, token) }
        val approverDTO = approver?.let { ApproverDTO(id = it.id, name = it.name) }
        
        // Buscar allocations do team para calcular memberCount e allocatedHours
        val allocations = allocationRepository.findAllByTeamId(team.id!!)
        val memberCount = allocations.size
        val allocatedHours = allocations.sumOf { it.allocatedHours }
        val totalHours = team.sprintDuration * 40
        val capacity = totalHours
        
        println("TeamService.toTeamResponseDTO: Team ${team.id} - sprintDuration=${team.sprintDuration}, memberCount=$memberCount, allocatedHours=$allocatedHours, totalHours=$totalHours")
        
        return TeamResponseDTO(
            id = team.id!!,
            name = team.name,
            description =  team.description,
            sprintDuration = team.sprintDuration,
            approverId = team.approverId,
            projectId = team.projectId,
            status = team.status,
            approver = approverDTO,
            memberCount = memberCount,
            allocatedHours = allocatedHours,
            totalHours = totalHours,
            capacity = capacity
        )
    }

    fun findAllTeamsPageable(pageable: Pageable, token: String): Page<TeamResponseDTO> {
        val teamsPage = teamRepository.findAll(pageable)
        return teamsPage.map { toTeamResponseDTO(it, token) }
    }

    fun findAllTeamsByProjectId(projectId: Long, token: String): List<TeamResponseDTO> {
        val teams = teamRepository.findAllByProjectId(projectId)
        println("TeamService.findAllTeamsByProjectId: Projeto $projectId - Encontrados ${teams.size} teams")
        return teams.map { toTeamResponseDTO(it, token) }
    }
}

