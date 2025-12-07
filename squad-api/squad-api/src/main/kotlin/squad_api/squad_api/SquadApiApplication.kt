package squad_api.squad_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SessionAutoConfiguration::class])
class SquadApiApplication

fun main(args: Array<String>) {
	runApplication<SquadApiApplication>(*args)
}
