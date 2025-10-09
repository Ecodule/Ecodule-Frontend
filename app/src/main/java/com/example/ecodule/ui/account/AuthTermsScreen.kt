package com.example.ecodule.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodule.ui.terms.TermsText

@Composable
fun AuthTermsScreen(
    onBack: () -> Unit,
    onAgreeAndBack: () -> Unit, // 追加: 同意して戻る（チェックON + 戻る）
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // サインアップ用ヘッダー（設定画面とは異なるシンプルなレイアウト）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "戻る",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "利用規約",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 本文（本文は設定と共通：TermsText.body を参照）
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = TermsText.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = TermsText.body,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }

        // フッター（サインアップ導線用）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAgreeAndBack, // 同意して戻る → 親でチェックONにして戻る
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342))
            ) {
                Text("同意して戻る", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}