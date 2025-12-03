package squad_api.squad_api.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import squad_api.squad_api.domain.model.Team

interface TeamRepository: JpaRepository<Team, Long>  {
    fun findAllByStatusTrue(pageable: Pageable): Page<Team>
}