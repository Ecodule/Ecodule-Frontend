package com.example.ecodule.ui.account.component

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.viewModelScope
import com.example.ecodule.BuildConfig
import com.example.ecodule.ui.account.api.googleAuthApi
import com.example.ecodule.ui.account.api.googleAuthApiResult
import com.example.ecodule.ui.account.model.LoginViewModel
import com.example.ecodule.ui.account.util.generateSecureRandomNonce
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.security.SecureRandom
import java.util.Base64

@Composable
fun GoogleAuthButton(
    text: String, // ★ ボタンのテキストを引数で受け取る
    loginViewModel: LoginViewModel,
    modifier: Modifier = Modifier // ★ Modifierを引数で受け取る
) {
    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    // 同時実行防止
    var isRunning by remember { mutableStateOf(false) } // 再コンポーズでの多重起動防止

    Log.d("ButtonUI", "generated nonce for this session: ${generateSecureRandomNonce()}")
    val onClick: () -> Unit = {
        Log.d("ButtonUI", "Button clicked $context, $activity")

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            if (isRunning) {
                // 既に処理中なら何もしない
                return@launch
            }
            isRunning = true

            if (activity == null) return@launch
            val result = googleAuthApi(
                activity = activity,
                request = request
            )

            isRunning = false

            Log.d("ButtonUI", "$result")

            when (result) {
                is googleAuthApiResult.Success -> {
                    Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "ID token (prefix): ${result.idToken.take(16)}…")
                }
                is googleAuthApiResult.NetworkError -> {
                    Toast.makeText(context, "ネットワークエラー", Toast.LENGTH_SHORT).show()
                }
                is googleAuthApiResult.Cancelled -> {
                    Toast.makeText(context, "キャンセルされました", Toast.LENGTH_SHORT).show()
                }
                is googleAuthApiResult.NoCredentials -> {
                    Toast.makeText(context, "有効なアカウントがありません", Toast.LENGTH_SHORT).show()
                }
                is googleAuthApiResult.Error -> {
                    Toast.makeText(context, "予期せぬエラーが発生", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    color = Color.Gray,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Google アイコン（簡略化）
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            Color(0xFF4285F4),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Text(
                        "G",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text, // ★ 引数で受け取ったテキストを使用
                    color = Color.Black,
                    fontSize = 16.sp,
                )
            }
        }
    }
}