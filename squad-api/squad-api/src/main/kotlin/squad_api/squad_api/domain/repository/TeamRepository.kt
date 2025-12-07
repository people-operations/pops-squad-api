package squad_api.squad_api.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import squad_api.squad_api.domain.model.Team

interface TeamRepository: JpaRepository<Team, Long>  {
    // Query nativa para garantir que funcione corretamente
    @Query(value = "SELECT * FROM team WHERE fk_project = :projectId", nativeQuery = true)
    fun findAllByProjectId(@Param("projectId") projectId: Long): List<Team>
    
    fun findAllByStatusTrue(pageable: Pageable): Page<Team>
}