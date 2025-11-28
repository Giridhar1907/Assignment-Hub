package com.example.assignmenthub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_splash) // Custom splash layout

            val logo: ImageView = findViewById(R.id.splash_logo)
            val anim = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
            logo.startAnimation(anim)

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

                    val intent = if (isLoggedIn) {
                        Log.d("SplashActivity", "User is logged in, opening MainActivity")
                        Intent(this, MainActivity::class.java)
                    } else {
                        Log.d("SplashActivity", "User not logged in, opening LoginActivity")
                        Intent(this, LoginActivity::class.java)
                    }

                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("SplashActivity", "Exception in splash handler: ${e.message}")
                }
            }, 1500) // 1.5 second splash duration
        } catch (e: Exception) {
            Log.e("SplashActivity", "Exception in onCreate: ${e.message}")
        }
    }
}
