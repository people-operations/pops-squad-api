package squad_api.squad_api.infraestructure.utilities

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

abstract class CrudService<T : Any, ID : Any> {

    protected abstract val repository: JpaRepository<T, ID>

    fun findAll(): List<T> = repository.findAll()

    fun findAllPageable(pageable: Pageable): Page<T> = repository.findAll(pageable)

    fun findById(id: ID): T =
        repository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado com ID: $id")
            }

    @Transactional
    open fun save(entity: T): T = repository.save(entity)

    @Transactional
    open fun update(id: ID, entity: T): T {
        if (!repository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado com ID: $id")
        }
        return repository.save(entity)
    }

    @Transactional
    open fun delete(id: ID) {
        if (!repository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado com ID: $id")
        }
        repository.deleteById(id)
    }
}