package squad_api.squad_api.domain.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "allocation_history")
data class AllocationHistory (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "allocated_hours", nullable = false)
    val allocatedHours: Int,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Column(name = "person_id", nullable = false)
    val personId: Long,

    @Column(name = "position", nullable = false)
    val position: String,

    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDate,

    @Column(name = "ended_at", nullable = false)
    val endedAt: LocalDate,
)