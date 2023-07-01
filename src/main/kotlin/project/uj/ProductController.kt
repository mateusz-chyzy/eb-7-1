package project.uj

import io.ktor.server.plugins.* // ktlint-disable no-wildcard-imports
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ProductDTO(
    val id: String?,
    val name: String,
    val price: Float,
    val img: String,
    val categoryId: String?
)

data class ShoppingCartDTO(
    val id: String?,
    val name: String,
    val price: Float,
    val categoryId: String?
)

object Product : Table("Product") {
    val id = uuid("id").primaryKey()
    val name = varchar("name", 255)
    val price = float("price")
    val img = varchar("img", 255).default("")
    val categoryId = (uuid("categoryId") references Categories.id).nullable()
}

object ShoppingCart : Table("shoppingcart") {
    val id = uuid("id").primaryKey()
    val name = varchar("name", 255)
    val price = float("price")
    val categoryId = (uuid("categoryId") references Categories.id).nullable()
}

object Categories : Table() {
    val id = uuid("id").primaryKey()
    val name = varchar("name", 255)
}

class ProductController(private val database: Database) {
    fun getAll(): List<ProductDTO> {
        return transaction(database) {
            Product.selectAll().map { toDTO(it) }
        }
    }

    fun getById(id: String): ProductDTO {
        return transaction(database) {
            Product.select { Product.id eq UUID.fromString(id) }
                .mapNotNull { toDTO(it) }
                .singleOrNull() ?: throw NotFoundException()
        }
    }

    fun create(productDTO: ProductDTO): ProductDTO {
        val id = UUID.randomUUID()
        transaction(database) {
            Product.insert {
                it[Product.id] = id
                it[name] = productDTO.name
                it[price] = productDTO.price
                it[img] = productDTO.img
                it[categoryId] = productDTO.categoryId?.let { UUID.fromString(it) }
            }
        }
        return productDTO.copy(id = id.toString())
    }

    fun createShoppingCart(shoppingCartDTOs: Array<ShoppingCartDTO>): Array<ShoppingCartDTO> {
        transaction(database) {
            for (shoppingCartDTO in shoppingCartDTOs) {
                val id = UUID.randomUUID()
                ShoppingCart.insert {
                    it[ShoppingCart.id] = id
                    it[name] = shoppingCartDTO.name
                    it[price] = shoppingCartDTO.price
                    it[categoryId] = null
                }
            }
        }
        return shoppingCartDTOs
    }

    fun update(id: String, productDTO: ProductDTO): ProductDTO {
        transaction(database) {
            val rowsUpdated = Product.update({ Product.id eq UUID.fromString(id) }) {
                it[name] = productDTO.name
                it[price] = productDTO.price
                it[img] = productDTO.img
                it[categoryId] = productDTO.categoryId?.let { UUID.fromString(it) }
            }
            if (rowsUpdated == 0) {
                throw NotFoundException()
            }
        }
        return productDTO.copy(id = id)
    }

    fun delete(id: String) {
        transaction(database) {
            val rowsDeleted = Product.deleteWhere { Product.id eq UUID.fromString(id) }
            if (rowsDeleted == 0) {
                throw NotFoundException()
            }
        }
    }

    private fun toDTO(row: ResultRow): ProductDTO {
        return ProductDTO(
            id = row[Product.id].toString(),
            name = row[Product.name],
            price = row[Product.price],
            img = row[Product.img],
            categoryId = row[Product.categoryId]?.toString()
        )
    }
}
