package squad_api.squad_api.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "team")
data class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 60)
    val name: String,

    @Column(length = 100)
    val description: String? = null,

    @Column(name = "sprint_duration", nullable = false)
    val sprintDuration: Int,

    @Column(name = "fk_approver")
    val approverId: Long? = null,

    @Column(name = "fk_project", nullable = false)
    val projectId: Long,

    @Column(nullable = false)
    val status: Boolean = true
)
