package com.example.ecodule.ui.account

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodule.R
import com.example.ecodule.ui.account.component.GoogleAuthButton
import com.example.ecodule.ui.account.model.AccountCreateViewModel
import com.example.ecodule.ui.account.util.EmailValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCreateScreen(
    accountCreateViewModel: AccountCreateViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
//    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var confirmPasswordFocused by remember { mutableStateOf(false) }
    var usernameFocused by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // メールアドレス検証
    val isValidEmailAddress = EmailValidator.isValidEmailForSignup(email)
    val showEmailError = email.isNotBlank() && !isValidEmailAddress

    // パスワード一致チェック
    val passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()
    val showPasswordError = !passwordsMatch && confirmPassword.isNotEmpty()

    // アカウント作成ボタンの有効性をチェック
    val isCreateEnabled = email.isNotBlank() &&
            isValidEmailAddress &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
//            username.isNotBlank() &&
            passwordsMatch &&
            termsAccepted

    // ViewModelの状態を監視
    val accountCreateError = accountCreateViewModel.accountCreateError
    val accountCreateMessage = accountCreateViewModel.accountCreateMessage


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
                .padding(top = 60.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Spacer(modifier = Modifier.height(60.dp))

            // Ecoduleロゴ（サイズを大きく）
            Image(
                painter = painterResource(id = R.drawable.ecodule_icon_tab),
                contentDescription = "Ecodule Logo",
                modifier = Modifier.size(width = 280.dp, height = 70.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // アカウント作成タイトル
            Text(
                text = "アカウント作成",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // メールアドレス入力フィールド
            Text(
                text = "メールアドレス",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
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
                    imeAction = ImeAction.Next
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

            Spacer(modifier = Modifier.height(16.dp))

            // パスワード入力フィールド
            Text(
                text = "パスワード",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                fontSize = 16.sp,
                color = Color.Gray
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { passwordFocused = it.isFocused },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    CustomPasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
                    focusedBorderColor = if (showPasswordError) Color.Red else Color(0xFF7CB342),
                    unfocusedBorderColor = if (showPasswordError) Color.Red else Color.LightGray,
                    cursorColor = Color(0xFF7CB342)
                ),
                shape = RoundedCornerShape(8.dp),
                isError = showPasswordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // パスワード確認入力フィールド
            Text(
                text = "パスワード（確認用）",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                fontSize = 16.sp,
                color = Color.Gray
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { confirmPasswordFocused = it.isFocused },
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    CustomPasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "パスワードを非表示" else "パスワードを表示",
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (showPasswordError) Color.Red else Color(0xFF7CB342),
                    unfocusedBorderColor = if (showPasswordError) Color.Red else Color.LightGray,
                    cursorColor = Color(0xFF7CB342)
                ),
                shape = RoundedCornerShape(8.dp),
                isError = showPasswordError
            )

            // パスワードエラーメッセージ
            if (showPasswordError) {
                Text(
                    text = "パスワードが異なっています",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ユーザー名入力フィールド
//            Text(
//                text = "ユーザー名",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 6.dp),
//                fontSize = 16.sp,
//                color = Color.Gray
//            )
//            OutlinedTextField(
//                value = username,
//                onValueChange = { username = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .onFocusChanged { usernameFocused = it.isFocused },
//                keyboardOptions = KeyboardOptions(
//                    imeAction = ImeAction.Done
//                ),
//                singleLine = true,
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = Color(0xFF7CB342),
//                    unfocusedBorderColor = Color.LightGray,
//                    cursorColor = Color(0xFF7CB342)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            )

            Spacer(modifier = Modifier.height(20.dp))

            // 利用規約同意チェックボックス
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // チェックボックスのみクリック可能
                Icon(
                    imageVector = if (termsAccepted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (termsAccepted) "チェック済み" else "未チェック",
                    tint = if (termsAccepted) Color(0xFF7CB342) else Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { termsAccepted = !termsAccepted }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    Text(
                        text = "利用規約",
                        color = Color(0xFF2196F3),
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            // TODO: 利用規約画面への遷移を実装
                        }
                    )
                    Text(
                        text = "に同意する",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* 登録エラーメッセージ */

            if (accountCreateError.value?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = accountCreateError.value ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            /* 登録完了メッセージ */
            if (accountCreateMessage.value.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = accountCreateMessage.value ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // アカウント作成ボタン
            Button(
                onClick = {
                    if (isCreateEnabled) {
                        Log.d("AccountCreate", "now creation")
                        accountCreateViewModel.accountCreate(email, password)
                        Log.d("AccountCreate", "after creation")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isCreateEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCreateEnabled) Color(0xFF7CB342) else Color.LightGray,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (!accountCreateViewModel.isLoading.value) {
                    Text(
                        "ログイン",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Googleで作成ボタン
            GoogleAuthButton(
                text = "Google で作成",
                onClick = { accountCreateViewModel.googleAccountCreate() }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// サーバーにアカウント情報を保存する関数（今後実装予定）
private fun saveAccountToServer(email: String, password: String, username: String) {
    // TODO: サーバーAPIを呼び出してアカウント情報を保存
    // 例: ApiClient.createAccount(email, password, username)
}

@Preview
@Composable
fun PreviewScr(){
    AccountCreateScreen(onBackToLogin = {})
}