package com.example.assignmenthub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var login: Button
    private lateinit var register: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        login = findViewById(R.id.btn_login_act)
        register = findViewById(R.id.btn_register)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        register.setOnClickListener {
            val email = findViewById<EditText>(R.id.email).text.toString().trim()
            val password = findViewById<EditText>(R.id.password).text.toString().trim()
            val number = findViewById<EditText>(R.id.phone).text.toString().trim()
            val regd = findViewById<EditText>(R.id.reg).text.toString().trim()
            val username = findViewById<EditText>(R.id.name).text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password must not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""

                        // Create a user map
                        val userMap = mapOf(
                            "email" to email,
                            "number" to number,
                            "regd" to regd,
                            "username" to username
                        )

                        // Save user info in Realtime Database under "users/userId"
                        val userRef = database.reference.child("users").child(userId)
                        userRef.setValue(userMap)
                            .addOnSuccessListener {
//                                // Save data in SharedPreferences (unchanged)
//                                val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//                                val editor = sharedPref.edit()
//
//                                editor.putString("email", email)
//                                editor.putString("password", password)  // Ideally don't store plain password here!
//                                editor.putString("number", number)
//                                editor.putString("regd", regd)
//                                editor.apply()

                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Navigate to MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
