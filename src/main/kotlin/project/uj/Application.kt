package project.uj

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import project.uj.plugins.configureRouting
import project.uj.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
