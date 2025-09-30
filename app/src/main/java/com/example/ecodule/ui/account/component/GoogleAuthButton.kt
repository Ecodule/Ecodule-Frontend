package com.example.ecodule.ui.account.component

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ecodule.ui.account.api.googleAuthApi
import com.example.ecodule.ui.account.model.LoginViewModel
import com.example.ecodule.ui.account.util.generateSecureRandomNonce
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64

@Composable
fun GoogleAuthButton(
    text: String, // ★ ボタンのテキストを引数で受け取る
    webClientId: String,
    loginViewModel: LoginViewModel,
    modifier: Modifier = Modifier // ★ Modifierを引数で受け取る
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Log.d("GoogleAuthButton", "Web Client ID: $webClientId")

    val onClick: () -> Unit = {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            googleAuthApi(request, context)
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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
                fontSize = 16.sp
            )
        }
    }
}