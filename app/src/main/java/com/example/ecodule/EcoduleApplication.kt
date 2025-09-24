package com.example.ecodule

import android.app.Application
import com.ecodule.android.security.SecureTokenManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class EcoduleApplication : Application() {

    @Inject
    lateinit var tokenManager: SecureTokenManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // セキュリティマネージャーの初期化
        applicationScope.launch {
            try {
                tokenManager.initialize()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}