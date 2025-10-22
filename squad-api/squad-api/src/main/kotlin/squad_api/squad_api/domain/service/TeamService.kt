package squad_api.squad_api.domain.service

import org.springframework.stereotype.Service
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.TeamRepository
import squad_api.squad_api.infraestructure.utilities.CrudService

@Service
class TeamService(
    private val teamRepository: TeamRepository
) : CrudService<Team, Long>() {

    override val repository = teamRepository

    init {
        requireNotNull(teamRepository) { "TeamRepository não pode ser nulo" }
    }
}