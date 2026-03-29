package com.quick.bite

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val nameInput = findViewById<EditText>(R.id.et_login_name)
        val continueButton = findViewById<MaterialButton>(R.id.btn_login_continue)

        continueButton.setOnClickListener {
            val userName = nameInput.text.toString().trim()
            if (userName.isEmpty()) {
                Toast.makeText(this, R.string.enter_name_prompt, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val welcomeMessage = getString(R.string.welcome_template, userName)
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_USER_NAME, userName)
                putExtra(MainActivity.EXTRA_WELCOME_MESSAGE, welcomeMessage)
            }
            startActivity(intent)
            finish()
        }
    }
}
