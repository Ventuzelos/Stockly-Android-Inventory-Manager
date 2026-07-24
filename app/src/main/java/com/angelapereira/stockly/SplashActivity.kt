package com.angelapereira.stockly

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashContent = findViewById<android.view.View>(R.id.splashContent)
        val splashFooter = findViewById<android.view.View>(R.id.splashFooter)

        splashContent.alpha = 0f
        splashContent.scaleX = 0.92f
        splashContent.scaleY = 0.92f

        splashContent.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        splashFooter.alpha = 0f
        splashFooter.animate()
            .alpha(0.8f)
            .setStartDelay(350)
            .setDuration(500)
            .start()

        splashContent.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1800)
    }
}