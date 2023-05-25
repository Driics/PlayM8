package ru.driics.playm8.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            true
        }
        super.onCreate(savedInstanceState, persistentState)

        startActivity(
            Intent(this, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
        )

        finishAndRemoveTask()
        overridePendingTransition(0, 0)
    }
}