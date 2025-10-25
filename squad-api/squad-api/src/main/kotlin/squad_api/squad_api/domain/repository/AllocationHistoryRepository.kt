package squad_api.squad_api.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import squad_api.squad_api.domain.model.AllocationHistory

interface AllocationHistoryRepository: JpaRepository<AllocationHistory, Long>  {
}