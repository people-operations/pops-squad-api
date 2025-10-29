package squad_api.squad_api.domain.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.ApproverDTO
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.TeamRepository
import squad_api.squad_api.infraestructure.utilities.CrudService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient

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
        return TeamResponseDTO(
            id = team.id!!,
            name = team.name,
            description =  team.description,
            sprintDuration = team.sprintDuration,
            approverId = team.approverId,
            projectId = team.projectId,
            status = team.status,
            approver = approverDTO
        )
    }

    fun findAllTeamsPageable(pageable: Pageable, token: String): Page<TeamResponseDTO> {
        val teamsPage = teamRepository.findAll(pageable)
        return teamsPage.map { toTeamResponseDTO(it, token) }
    }
}

