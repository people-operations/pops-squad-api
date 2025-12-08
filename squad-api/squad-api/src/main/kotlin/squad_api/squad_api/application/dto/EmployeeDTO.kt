package squad_api.squad_api.application.dto

data class EmployeeDTO(
    val id: Long,
    val name: String,
    val jobTitle: String? = null,
    val contractWage: Double? = null,
    val workHoursPerWeek: Int? = null,
    val skills: List<EmployeeSkillDTO> = emptyList()
)

data class EmployeeSkillDTO(
    val id: Int,
    val name: String,
    val skillType: EmployeeSkillTypeDTO? = null
)

data class EmployeeSkillTypeDTO(
    val id: Int? = null,
    val name: String? = null
)