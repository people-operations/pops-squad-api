package squad_api.squad_api.application.dto

import java.math.BigDecimal

data class TeamDetailDTO(
    val teamId: Long,
    val teamName: String,
    val teamDescription: String?,
    val po: String?,
    val membersCount: Int,
    val members: List<MemberDetailDTO>,
    val totalInvestedValue: BigDecimal,
    val skills: List<SkillDetailDTO> = emptyList(),
    val totalHours: Int? = null,
    val allocatedHours: Int? = null,
    val sprintDuration: Int? = null
)

data class MemberDetailDTO(
    val id: Long,
    val name: String,
    val jobTitle: String?,
    val allocatedHours: Int,
    val skills: List<MemberSkillDetailDTO>,
    val contractWage: BigDecimal?,
    val workHoursPerWeek: Int?,
    val monthlyHours: BigDecimal?,
    val hourlyRate: BigDecimal?,
    val investedValue: BigDecimal?
)

data class MemberSkillDetailDTO(
    val id: Int,
    val name: String,
    val skillType: String? = null
)

data class SkillDetailDTO(
    val id: Int,
    val name: String,
    val skillType: String? = null
)

