package com.angelapereira.stockly

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton

    private val validCredentials = mapOf(
        "admin" to "password123",
        "cesae" to "cesae",
        "angela" to "stockly2026"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        usernameInputLayout = findViewById(R.id.usernameInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            validateLogin()
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateLogin()
                true
            } else {
                false
            }
        }
    }

    private fun validateLogin() {
        clearErrors()

        val username = usernameEditText.text?.toString()?.trim().orEmpty()
        val password = passwordEditText.text?.toString().orEmpty()

        when {
            username.isBlank() -> {
                usernameInputLayout.error = getString(R.string.login_error_empty_username)
                usernameEditText.requestFocus()
            }

            password.isBlank() -> {
                passwordInputLayout.error = getString(R.string.login_error_empty_password)
                passwordEditText.requestFocus()
            }

            validCredentials[username] == password -> {
                openMainActivity(username)
            }

            else -> {
                Toast.makeText(
                    this,
                    getString(R.string.login_error_invalid_credentials),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearErrors() {
        usernameInputLayout.error = null
        passwordInputLayout.error = null
    }

    private fun openMainActivity(username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_USERNAME, username)
        }

        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }
}