package com.example.ecodule.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ecodule.ui.account.EmailValidator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onPasswordResetSent: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailFocused by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var isEmailSent by remember { mutableStateOf(false) }

    // メールアドレス検証
    val isValidEmailAddress = EmailValidator.isValidEmail(email)
    val isSendEnabled = email.isNotBlank() && isValidEmailAddress // メールアドレスが空でなく、かつ有効な場合のみ
    val showEmailError = email.isNotBlank() && !isValidEmailAddress

    // カウントダウンタイマー
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // ドラッグ終了時の処理
                    }
                ) { change, dragAmount ->
                    // 左から右へのスワイプを検出（50px以上の右方向のドラッグ）
                    if (dragAmount > 50f) {
                        onBackToLogin()
                    }
                }
            }
    ) {
        // 戻るボタン
        IconButton(
            onClick = onBackToLogin,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "戻る",
                tint = Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // タイトル
            Text(
                text = "パスワードを再設定",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 60.dp)
            )

            // メールアドレス入力フィールド
            Text(
                text = "メールアドレス",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                fontSize = 16.sp,
                color = Color.Gray
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { emailFocused = it.isFocused },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
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

            Spacer(modifier = Modifier.height(24.dp))

            // 説明文
            Text(
                text = "パスワード再設定用のメールを登録メールアドレスに送信します",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "パスワードの再設定はWebブラウザ上から行って下さい",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(80.dp))

            // 認証メール送信ボタン
            Button(
                onClick = {
                    if (isSendEnabled) {
                        showDialog = true
                        isEmailSent = true
                        countdown = 30
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isSendEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSendEnabled) Color(0xFF7CB342) else Color.LightGray,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "認証メールを送信",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 確認ダイアログ
        if (showDialog) {
            Dialog(onDismissRequest = { }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "メールをご確認ください",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "パスワード再設定用のメールを登録メールアドレスに送信しました。\n送信したメールのURLからパスワードの再設定を行って下さい。",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp),
                            lineHeight = 20.sp
                        )

                        // 再送信ボタン
                        OutlinedButton(
                            onClick = {
                                if (countdown == 0) {
                                    countdown = 30
                                    // TODO: メール再送信処理
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp), // ログイン画面へボタンと同じ高さに統一
                            enabled = countdown == 0,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (countdown == 0) Color.Gray else Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (countdown > 0) "($countdown" + "s)  再送信する" else "再送信する",
                                color = if (countdown == 0) Color.Black else Color.Gray,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // ボタン間のスペースを統一

                        // ログイン画面へボタン
                        Button(
                            onClick = {
                                showDialog = false
                                onBackToLogin()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp), // 再送信ボタンと同じ高さに統一
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7CB342)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "ログイン画面へ",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}