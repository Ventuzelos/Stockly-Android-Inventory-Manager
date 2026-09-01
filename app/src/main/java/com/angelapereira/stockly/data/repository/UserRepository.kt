package com.angelapereira.stockly.data.repository

import com.angelapereira.stockly.data.local.User
import com.angelapereira.stockly.data.local.UserDao
import java.security.MessageDigest

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun login(
        username: String,
        password: String
    ): Boolean {

        val user = userDao.getUserByUsername(username)
            ?: return false

        return hashPassword(password) == user.passwordHash
    }

    suspend fun createDefaultUsers() {

        if (userDao.getUserCount() > 0) {
            return
        }

        userDao.insert(
            User(
                username = "admin",
                passwordHash = hashPassword("password123")
            )
        )

        userDao.insert(
            User(
                username = "cesae",
                passwordHash = hashPassword("cesae")
            )
        )

        userDao.insert(
            User(
                username = "angela",
                passwordHash = hashPassword("stockly2026")
            )
        )
    }

    private fun hashPassword(password: String): String {

        val digest = MessageDigest.getInstance("SHA-256")

        val hash = digest.digest(
            password.toByteArray(Charsets.UTF_8)
        )

        return hash.joinToString("") {
            "%02x".format(it)
        }
    }
}