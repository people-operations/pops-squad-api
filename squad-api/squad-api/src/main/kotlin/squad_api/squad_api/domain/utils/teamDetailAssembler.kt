package squad_api.squad_api.domain.utils

import org.springframework.stereotype.Component
import squad_api.squad_api.application.dto.MemberDetailDTO
import squad_api.squad_api.application.dto.MemberSkillDetailDTO
import squad_api.squad_api.application.dto.SkillDetailDTO
import squad_api.squad_api.application.dto.TeamDetailDTO
import squad_api.squad_api.domain.model.Allocation
import squad_api.squad_api.domain.model.Team
import squad_api.squad_api.domain.repository.AllocationRepository
import squad_api.squad_api.domain.service.PopsSrvEmployeeClient
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class TeamDetailAssembler(
    private val popsSrvEmployeeClient: PopsSrvEmployeeClient,
    private val allocationRepository: AllocationRepository
) {

    fun toTeamDetailDTO(team: Team, token: String): TeamDetailDTO {
        val allocations = allocationRepository.findAllByTeamId(team.id!!)
        println("[INFO] - Detalhes: ${allocations}")
        val membersDetail = buildMembersDetail(allocations, token)
        println("[INFO] - Detalhes dos membros: ${membersDetail}")

        val totalInvestedValue = calculateTotalInvestedValue(membersDetail)
        val aggregatedSkills = aggregateSkills(membersDetail)
        val approverName = resolveApproverName(team, token)
        val totalHours = team.sprintDuration * 40
        val allocatedHours = membersDetail.sumOf { it.allocatedHours }

        println("TeamDetailAssembler: teamId=${team.id}, members=${membersDetail.size}")

        return TeamDetailDTO(
            teamId = team.id!!,
            teamName = team.name,
            teamDescription = team.description,
            po = approverName,
            membersCount = membersDetail.size,
            members = membersDetail,
            totalInvestedValue = totalInvestedValue,
            skills = aggregatedSkills,
            totalHours = totalHours,
            allocatedHours = allocatedHours,
            sprintDuration = team.sprintDuration
        )
    }

    private fun buildMembersDetail(
        allocations: List<Allocation>,
        token: String
    ): List<MemberDetailDTO> {
        return allocations.mapNotNull { allocation ->
            try {
                buildMemberDetail(allocation, token)
            } catch (e: Exception) {
                println("Erro ao processar membro ${allocation.personId}: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    private fun buildMemberDetail(
        allocation: Allocation,
        token: String
    ): MemberDetailDTO? {
        println("cheguei ate aqui pelo menos")
        println("Buscando detalhes para employeeId=${allocation.personId}")
        val employee = popsSrvEmployeeClient.findEmployeeById(allocation.personId, token) ?: return null
        print(popsSrvEmployeeClient.findEmployeeById(allocation.personId, token))
        println("Building MemberDetailDTO for employeeId=${employee.id}, name=${employee.name}")

        val weeklyHours = employee.workHoursPerWeek ?: 40
        val monthlyHoursDecimal = BigDecimal(weeklyHours * 4).setScale(2, RoundingMode.HALF_UP)

        val contractWageDecimal = employee.contractWage
            ?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP) }

        val hourlyRate = if (contractWageDecimal != null && monthlyHoursDecimal > BigDecimal.ZERO) {
            contractWageDecimal.divide(monthlyHoursDecimal, 2, RoundingMode.HALF_UP)
        } else {
            null
        }

        val investedValue = if (hourlyRate != null && allocation.allocatedHours > 0) {
            hourlyRate.multiply(BigDecimal(allocation.allocatedHours))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            null
        }

        val memberSkills = employee.skills.map { skill ->
            MemberSkillDetailDTO(
                id = skill.id,
                name = skill.name,
                skillType = skill.skillType?.name
            )
        }

        return MemberDetailDTO(
            id = employee.id,
            name = employee.name,
            jobTitle = employee.jobTitle,
            allocatedHours = allocation.allocatedHours,
            skills = memberSkills,
            contractWage = contractWageDecimal,
            workHoursPerWeek = employee.workHoursPerWeek,
            monthlyHours = monthlyHoursDecimal,
            hourlyRate = hourlyRate,
            investedValue = investedValue
        )
    }

    private fun calculateTotalInvestedValue(members: List<MemberDetailDTO>): BigDecimal {
        return members
            .mapNotNull { it.investedValue }
            .fold(BigDecimal.ZERO) { acc, value -> acc.add(value) }
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun aggregateSkills(members: List<MemberDetailDTO>): List<SkillDetailDTO> {
        val allSkillsMap = mutableMapOf<Int, SkillDetailDTO>()
        members.forEach { member ->
            member.skills.forEach { skill ->
                if (!allSkillsMap.containsKey(skill.id)) {
                    allSkillsMap[skill.id] = SkillDetailDTO(
                        id = skill.id,
                        name = skill.name,
                        skillType = skill.skillType
                    )
                }
            }
        }
        return allSkillsMap.values.toList()
    }

    private fun resolveApproverName(team: Team, token: String): String? {
        val approverId = team.approverId ?: return null
        val approverFull = popsSrvEmployeeClient.findEmployeeById(approverId, token) ?: return null
        return approverFull.name
    }
}
