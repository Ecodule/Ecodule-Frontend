package com.example.ecodule.ui.settings.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingTermsScreen(
    modifier: Modifier = Modifier,
    onBackToDetails: () -> Unit = {},
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                    },
                    onDragEnd = {
                        // 左から右へのスワイプ（100px以上の右方向のドラッグ）を検出
                        if (totalDragX > 100f) {
                            onBackToDetails()
                        }
                        totalDragX = 0f
                    }
                ) { _, dragAmount ->
                    totalDragX += dragAmount.x
                }
            }
    ) {
        // ヘッダー部分
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 戻るボタン（左揃え）
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackToDetails() }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "戻る",
                    tint = Color(0xFF95cf4d),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "詳細",
                    color = Color(0xFF95cf4d),
                    fontSize = 14.sp
                )
            }

            // タイトル（中央揃え）
            Text(
                text = "利用規約",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // コンテンツ本体（スクロール可能）
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 簡易版 利用規約
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "カレンダーアプリ 利用規約（簡易版）",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val termsText = """
                        本規約は、本アプリ（以下「本アプリ」）の提供条件および本アプリの利用に関する、当社とユーザーとの間の権利義務関係を定めるものです。ユーザーは本アプリをインストールまたは使用することで、本規約に同意したものとみなします。

                        1. 定義
                        ・「ユーザー」とは、本アプリを利用するすべての者をいいます。
                        ・「コンテンツ」とは、ユーザーが本アプリに登録・作成・保存する予定、メモ、画像等の一切をいいます。

                        2. 提供内容
                        本アプリは、予定の作成・編集・削除、リマインダー、通知等の機能を提供します。機能は予告なく追加・変更・終了することがあります。

                        3. アカウント・データ
                        ・本アプリに保存されたデータはユーザー自身でバックアップしてください。
                        ・端末の紛失・故障、アプリの不具合等によりデータが消失・破損した場合でも、当社は一切の責任を負いません。

                        4. 禁止事項
                        ・法令または公序良俗に違反する行為
                        ・他者の権利を侵害する行為
                        ・本アプリの運営を妨害する行為 など

                        5. 免責
                        当社は、本アプリに事実上または法律上の瑕疵がないことを明示的にも黙示的にも保証しません。本アプリの利用により生じたいかなる損害についても、当社は一切の責任を負いません。

                        6. 規約の変更
                        当社は必要に応じて本規約を変更できます。変更後の規約は本アプリ内に表示した時点から効力を生じます。

                        7. 準拠法・裁判管轄
                        本規約は日本法に準拠し、本アプリに関して生じた紛争は、当社の本店所在地を管轄する裁判所を第一審の専属的合意管轄とします。

                        最終更新日: 2025-10-09
                    """.trimIndent()

                    Text(
                        text = termsText,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 改行の見え方の例
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "改行表示の例",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val newlineExample = """
                        これは1行目です。
                        これは2行目です。

                        ここで段落を分けます（空行＝\n\n）。
                        ・箇条書き1
                        ・箇条書き2

                        1行内で軽い改行をしたい場合は「\n」を使います。
                        段落を分けたい場合は「\n\n」のように空行を入れてください。
                    """.trimIndent()

                    Text(
                        text = newlineExample,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}