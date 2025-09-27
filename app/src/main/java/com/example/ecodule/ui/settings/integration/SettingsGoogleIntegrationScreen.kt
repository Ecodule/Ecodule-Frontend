package com.example.ecodule.ui.settings.integration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsGoogleIntegrationScreen(
    modifier: Modifier = Modifier,
    onBackToSettings: () -> Unit = {}
) {
    // 連携状態を管理するState
    var isGoogleAccountLinked by remember { mutableStateOf(false) }
    var googleUserName by remember { mutableStateOf("") }
    var googleUserEmail by remember { mutableStateOf("") }
    
    // Googleカレンダーの連携はGoogleアカウント連携が前提
    val isGoogleCalendarEnabled = isGoogleAccountLinked

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Google カレンダーと連携",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackToSettings) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF444444)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Googleアカウント連携セクション
            GoogleAccountSection(
                isLinked = isGoogleAccountLinked,
                userName = googleUserName,
                userEmail = googleUserEmail,
                onLinkClick = {
                    if (isGoogleAccountLinked) {
                        // 削除処理（実装は省略）
                        isGoogleAccountLinked = false
                        googleUserName = ""
                        googleUserEmail = ""
                    } else {
                        // 連携処理（実装は省略）
                        isGoogleAccountLinked = true
                        googleUserName = "User Name"
                        googleUserEmail = "testuser@gmail.com"
                    }
                }
            )
            
            // 説明文
            Text(
                text = "Google カレンダーと連携するにはGoogleアカウントを紐づける必要があります",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
            
            // Googleカレンダー連携セクション
            GoogleCalendarSection(
                isEnabled = isGoogleCalendarEnabled,
                isLinked = isGoogleAccountLinked && isGoogleCalendarEnabled, // 実際の連携状態
                onLinkClick = {
                    if (isGoogleCalendarEnabled) {
                        // Googleカレンダーの連携/削除処理（実装は省略）
                    }
                }
            )
        }
    }
}

@Composable
private fun GoogleAccountSection(
    isLinked: Boolean,
    userName: String,
    userEmail: String,
    onLinkClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_color_icon),
                    contentDescription = "Google",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Google アカウント",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF444444)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isLinked) {
                        Text(
                            text = userName,
                            fontSize = 14.sp,
                            color = Color(0xFF444444)
                        )
                        Text(
                            text = userEmail,
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    } else {
                        Text(
                            text = "紐づけされていません",
                            fontSize = 14.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onLinkClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLinked) Color(0xFFE53E3E) else Color(0xFF7CB342)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isLinked) "削除" else "連携",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GoogleCalendarSection(
    isEnabled: Boolean,
    isLinked: Boolean,
    onLinkClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Color.White else Color(0xFFF8F8F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_calendar_icon),
                    contentDescription = "Google Calendar",
                    modifier = Modifier.size(32.dp),
                    tint = if (isEnabled) Color.Unspecified else Color(0xFFCCCCCC)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Google カレンダー",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) Color(0xFF444444) else Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLinked) "連携済み" else "紐づけされていません",
                        fontSize = 14.sp,
                        color = if (isEnabled) Color(0xFF888888) else Color(0xFFCCCCCC)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onLinkClick,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLinked) Color(0xFFE53E3E) else Color(0xFF7CB342),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isLinked) "削除" else "連携",
                    color = if (isEnabled) Color.White else Color(0xFFAAAAAA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsGoogleIntegrationScreenPreview() {
    SettingsGoogleIntegrationScreen()
}