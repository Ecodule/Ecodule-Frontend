package com.example.ecodule.ui.account.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoogleAuthButton(
    text: String, // ★ ボタンのテキストを引数で受け取る
    onClick: () -> Unit,
    modifier: Modifier = Modifier // ★ Modifierを引数で受け取る
) {
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