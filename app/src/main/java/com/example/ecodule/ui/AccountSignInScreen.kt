package com.example.ecodule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSignInScreen(
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGuestMode: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    // メールアドレス検証
    val isValidEmail = email.isBlank() || (email.contains("@") && email.contains("."))
    val showEmailError = email.isNotBlank() && !isValidEmail

    // ログインボタンの有効性をチェック
    val isLoginEnabled = email.isNotBlank() && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(60.dp))

        // Ecoduleロゴ
        Image(
            painter = painterResource(id = R.drawable.ecodule_icon1),
            contentDescription = "Ecodule Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // メールアドレス入力フィールド
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    "メールアドレス",
                    color = if (emailFocused) Color(0xFF7CB342) else Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { emailFocused = it.isFocused },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (showEmailError) Color.Red else Color(0xFF7CB342),
                unfocusedBorderColor = if (showEmailError) Color.Red else Color.LightGray,
                cursorColor = Color(0xFF7CB342)
            ),
            shape = RoundedCornerShape(8.dp),
            isError = showEmailError
        )

        // メールアドレスエラーメッセージ
        if (showEmailError) {
            Text(
                text = "有効なメールアドレスではありません",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // パスワード入力フィールド
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    "パスワード",
                    color = if (passwordFocused) Color(0xFF7CB342) else Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { passwordFocused = it.isFocused },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                CustomPasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "パスワードを非表示" else "パスワードを表示",
                        tint = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7CB342),
                unfocusedBorderColor = Color.LightGray,
                cursorColor = Color(0xFF7CB342)
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // パスワードをお忘れですか？
        Text(
            text = "パスワードをお忘れですか？",
            color = Color(0xFF2196F3),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPassword() },
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ログインボタン
        Button(
            onClick = {
                if (isLoginEnabled) {
                    onLoginSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isLoginEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoginEnabled) Color(0xFF7CB342) else Color.LightGray,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "ログイン",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // アカウントをお持ちでないですか？
        Text(
            text = "アカウントをお持ちでないですか？",
            color = Color(0xFF2196F3),
            fontSize = 14.sp,
            modifier = Modifier.clickable { onSignUp() },
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(32.dp))

        // または
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
            Text(
                text = "または",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Googleで続行ボタン
        OutlinedButton(
            onClick = { onGoogleSignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
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
                    "Google で続行",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ゲストモードで続行
        TextButton(
            onClick = { onGuestMode() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "ゲストモードで続行",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// カスタムパスワード変換（最後の文字以外を*で表示）
class CustomPasswordVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val transformedText = if (text.text.isEmpty()) {
            ""
        } else {
            "*".repeat(text.text.length - 1) + text.text.last()
        }

        return TransformedText(
            androidx.compose.ui.text.AnnotatedString(transformedText),
            OffsetMapping.Identity
        )
    }
}