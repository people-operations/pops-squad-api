package squad_api.squad_api.infraestructure.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val httpStatus = HttpStatus.resolve(ex.statusCode.value()) ?: HttpStatus.INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse(
            status = ex.statusCode.value(),
            error = httpStatus.reasonPhrase,
            message = ex.reason ?: "Erro na requisição",
            path = request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity.status(ex.statusCode).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Erro interno do servidor",
            path = request.getDescription(false).replace("uri=", "")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

@RestController
class CustomErrorController : ErrorController {

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(response.status)
        val errorResponse = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = "Recurso não encontrado",
            path = request.requestURI ?: ""
        )
        return ResponseEntity.status(status).body(errorResponse)
    }
}

