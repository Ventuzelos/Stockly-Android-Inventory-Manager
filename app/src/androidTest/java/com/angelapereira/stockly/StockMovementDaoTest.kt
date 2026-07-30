package com.angelapereira.stockly

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.angelapereira.stockly.data.local.MovementType
import com.angelapereira.stockly.data.local.Product
import com.angelapereira.stockly.data.local.ProductDao
import com.angelapereira.stockly.data.local.StockMovement
import com.angelapereira.stockly.data.local.StockMovementDao
import com.angelapereira.stockly.data.local.StocklyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockMovementDaoTest {

    private lateinit var database: StocklyDatabase
    private lateinit var productDao: ProductDao
    private lateinit var stockMovementDao: StockMovementDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            StocklyDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        productDao = database.productDao()
        stockMovementDao = database.stockMovementDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun registerEntry_increasesProductQuantity() = runTest {
        val productId = productDao.insert(
            Product(
                name = "Teclado",
                category = "Periféricos",
                quantity = 10,
                minimumStock = 2
            )
        ).toInt()

        stockMovementDao.registerMovement(
            movement = StockMovement(
                productId = productId,
                type = MovementType.ENTRY,
                quantity = 5
            ),
            productDao = productDao
        )

        val updatedProduct = productDao.getProductById(productId)

        assertEquals(
            15,
            updatedProduct?.quantity
        )

        val movements = stockMovementDao
            .getMovementsByProduct(productId)
            .first()

        assertEquals(1, movements.size)
        assertEquals(MovementType.ENTRY, movements.first().type)
        assertEquals(5, movements.first().quantity)
    }

    @Test
    fun registerExit_decreasesProductQuantity() = runTest {
        val productId = productDao.insert(
            Product(
                name = "Rato",
                category = "Periféricos",
                quantity = 10,
                minimumStock = 2
            )
        ).toInt()

        stockMovementDao.registerMovement(
            movement = StockMovement(
                productId = productId,
                type = MovementType.EXIT,
                quantity = 4
            ),
            productDao = productDao
        )

        val updatedProduct = productDao.getProductById(productId)

        assertEquals(
            6,
            updatedProduct?.quantity
        )
    }

    @Test
    fun registerExit_aboveAvailableStock_throwsException() = runTest {
        val productId = productDao.insert(
            Product(
                name = "Monitor",
                category = "Ecrãs",
                quantity = 3,
                minimumStock = 1
            )
        ).toInt()

        var exceptionThrown = false

        try {
            stockMovementDao.registerMovement(
                movement = StockMovement(
                    productId = productId,
                    type = MovementType.EXIT,
                    quantity = 5
                ),
                productDao = productDao
            )
        } catch (exception: IllegalArgumentException) {
            exceptionThrown = true

            assertEquals(
                "Stock insuficiente.",
                exception.message
            )
        }

        assertTrue(exceptionThrown)

        val unchangedProduct = productDao.getProductById(productId)

        assertEquals(
            3,
            unchangedProduct?.quantity
        )

        val movements = stockMovementDao
            .getMovementsByProduct(productId)
            .first()

        assertTrue(movements.isEmpty())
    }

    @Test
    fun registerMovement_withZeroQuantity_throwsException() = runTest {
        val productId = productDao.insert(
            Product(
                name = "Cabo USB",
                category = "Acessórios",
                quantity = 8,
                minimumStock = 2
            )
        ).toInt()

        var exceptionThrown = false

        try {
            stockMovementDao.registerMovement(
                movement = StockMovement(
                    productId = productId,
                    type = MovementType.ENTRY,
                    quantity = 0
                ),
                productDao = productDao
            )
        } catch (exception: IllegalArgumentException) {
            exceptionThrown = true

            assertEquals(
                "A quantidade deve ser superior a zero.",
                exception.message
            )
        }

        assertTrue(exceptionThrown)
    }
}