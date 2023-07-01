package project.uj.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import project.uj.ProductController
import project.uj.ProductDTO
import project.uj.ShoppingCartDTO

fun Application.configureRouting() {
    val database = Database.connect("jdbc:postgresql://localhost:5434/shop", driver = "org.postgresql.Driver", user = "user", password = "password1234")
    val productController = ProductController(database)
    routing {
        get {
            val products = productController.getAll()
            call.respond(products)
        }
        get("/{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Missing id")
            val product = productController.getById(id)
            call.respond(product)
        }
        post {
            val product = call.receive<ProductDTO>()
            val newProduct = productController.create(product)
            call.respond(HttpStatusCode.Created, newProduct)
        }
        post("/shopping") {
            val shoppingCartDTO = call.receive<Array<ShoppingCartDTO>>()
            val ordered = productController.createShoppingCart(shoppingCartDTO)
            call.respond(HttpStatusCode.Created, ordered)
        }
        put("/{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Missing id")
            val product = call.receive<ProductDTO>()
            val updatedProduct = productController.update(id, product)
            call.respond(updatedProduct)
        }
        delete("/{id}") {
            val id = call.parameters["id"] ?: throw BadRequestException("Missing id")
            productController.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
