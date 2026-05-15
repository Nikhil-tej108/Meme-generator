package com.example.memegenerator1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

//        // Delay for 1 seconds
//        Thread.sleep(1000)
        // Delay for 1 second (show splash logo)
        lifecycleScope.launch {
            delay(1000) // 1 second
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))

            finish()
        }
    }
}
