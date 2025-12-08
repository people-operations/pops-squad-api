package squad_api.squad_api.domain.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import squad_api.squad_api.application.dto.TeamDetailDTO
import squad_api.squad_api.application.dto.TeamResponseDTO
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.TeamRepository
import squad_api.squad_api.domain.utils.TeamDetailAssembler
import squad_api.squad_api.domain.utils.TeamResponseAssembler
import squad_api.squad_api.infraestructure.utilities.CrudService

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamResponseAssembler: TeamResponseAssembler,
    private val teamDetailAssembler: TeamDetailAssembler
) : CrudService<Team, Long>() {

    override val repository: TeamRepository = teamRepository

    fun findTeamById(
        teamId: Long,
        token: String,
        allocations: List<Allocation>
    ): TeamResponseDTO {
        val team = getTeamOrThrow(teamId)
        val dto = teamResponseAssembler.toTeamResponseDTO(
            team = team,
            token = token,
            allocations = allocations
        )
        return dto
    }

    fun findAllTeamsPageable(
        pageable: Pageable,
        token: String,
        allocations: List<Allocation>
    ): Page<TeamResponseDTO> {
        val teamsPage = teamRepository.findAllByStatusTrue(pageable)
        return teamsPage.map { team ->
            teamResponseAssembler.toTeamResponseDTO(
                team = team,
                token = token,
                allocations = allocations
            )
        }
    }

    fun softDeleteTeam(id: Long) {
        val team = getTeamOrThrow(id)
        team.status = false
        repository.save(team)
    }

    fun findAllTeamsByProjectId(projectId: Long, token: String): List<TeamResponseDTO> {
        val teams = teamRepository.findAllByProjectId(projectId)
        return teams.map { team ->
            teamResponseAssembler.toTeamResponseDTO(
                team = team,
                token = token,
                allocations = emptyList() // aqui ele vai buscar allocations direto do repo
            )
        }
    }

    fun getTeamDetails(teamId: Long, token: String): TeamDetailDTO {
        val team = getTeamOrThrow(teamId)
        return try {
            teamDetailAssembler.toTeamDetailDTO(team, token)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            println("Erro ao buscar detalhes do team $teamId: ${e.message}")
            e.printStackTrace()
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao buscar detalhes do team: ${e.message}"
            )
        }
    }

    private fun getTeamOrThrow(teamId: Long): Team {
        return repository.findById(teamId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum time encontrado com ID: $teamId")
        }
    }
}