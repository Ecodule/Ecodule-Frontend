package com.example.ecodule.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ecodule.ui.statistics.model.StatisticsViewModel
import java.text.NumberFormat
import java.util.Locale

val bodyStyle = TextStyle(
    fontSize = 28.sp,
    color = Color.Black
)

val boldBodyStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    color = Color.Black
)

@Composable
fun StatisticsContent(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel? = null // プレビュー対応のためオプショナル
) {
    val isPreview = LocalInspectionMode.current
    // 実行時のみ Hilt から取得（プレビュー時は null のまま）
    val vm = viewModel ?: if (!isPreview) hiltViewModel() else null

    // 状態の購読（vm が null の場合はダミー表示にフォールバック）
    val isLoadingUser by (vm?.isLoadingUserStats?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf(false))
    val userError by (vm?.userStatsError?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf<String?>(null))
    val userStats by (vm?.userStats?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf<StatisticsViewModel.UserStatsUiState?>(null))

    val isLoadingOverall by (vm?.isLoadingOverallStats?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf(false))
    val overallError by (vm?.overallStatsError?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf<String?>(null))
    val overallStats by (vm?.overallStats?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf<StatisticsViewModel.OverallStatsUiState?>(null))

    // 初回取得
    LaunchedEffect(vm != null) {
        if (vm != null) {
            vm.fetchUserStatistics()
            vm.fetchOverallStatistics()
        }
    }

    // 表示用フォーマット
    val numberFormatter = NumberFormat.getNumberInstance(Locale.JAPAN).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    fun formatKg(value: Double?): String =
        value?.let { "${numberFormatter.format(it)} Kg" } ?: "-- Kg"

    fun formatYen(value: Double?): String =
        value?.let { "${numberFormatter.format(it)} 円" } ?: "-- 円"

    // 差分の表示は API 仕様にないため、ここでは空文字を渡し、将来仕様追加時に差し替え
    val savingDiffText = ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "統計",
            style = boldBodyStyle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // ユーザー統計（CO2）
        when {
            isLoadingUser -> {
                StatItemWithIcon(
                    icon = "🌱",
                    label = "CO₂削減量",
                    value = "読み込み中..."
                )
            }
            userError != null -> {
                Text(
                    text = userError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = bodyStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                StatItemWithIcon(
                    icon = "🌱",
                    label = "CO₂削減量",
                    value = formatKg(userStats?.co2Reduction)
                )
            }
            else -> {
                StatItemWithIcon(
                    icon = "🌱",
                    label = "CO₂削減量",
                    value = formatKg(userStats?.co2Reduction)
                )
            }
        }

        // ユーザー統計（金額）: 差分表示は空（API未対応）
        when {
            isLoadingUser -> {
                StatItemWithIconAndDifference(
                    icon = "💴",
                    label = "今月の節約額",
                    value = "読み込み中...",
                    difference = savingDiffText,
                    differenceColor = Color.Blue
                )
            }
            userError != null -> {
                StatItemWithIconAndDifference(
                    icon = "💴",
                    label = "今月の節約額",
                    value = formatYen(userStats?.moneySaved),
                    difference = savingDiffText,
                    differenceColor = Color.Blue
                )
            }
            else -> {
                StatItemWithIconAndDifference(
                    icon = "💴",
                    label = "今月の節約額",
                    value = formatYen(userStats?.moneySaved),
                    difference = savingDiffText,
                    differenceColor = Color.Blue
                )
            }
        }

        // 全体統計（全ユーザーCO2）
        when {
            isLoadingOverall -> {
                StatItemWithIcon(
                    icon = "🌱",
                    label = "全ユーザーのCO₂削減量",
                    value = "読み込み中..."
                )
            }
            overallError != null -> {
                Text(
                    text = overallError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = bodyStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                StatItemWithIcon(
                    icon = "🌱",
                    label = "全ユーザーのCO₂削減量",
                    value = formatKg(overallStats?.totalCo2Reduction)
                )
            }
            else -> {
                StatItemWithIcon(
                    icon = "🌱",
                    label = "全ユーザーのCO₂削減量",
                    value = formatKg(overallStats?.totalCo2Reduction)
                )
            }
        }

//        // 仕様次第でりんご表示を再開
//        StatItemWithIcon(
//            icon = "🍎",
//            label = "集めたりんごの数",
//            value = "${appleCount} 個"
//        )

        // ▼▼▼▼▼ ここから追加 ▼▼▼▼▼

        // このSpacerが後続の要素を画面下部に押し出す
        // このSpacerが後続の要素を画面下部に押し出す
        Spacer(modifier = Modifier.weight(1f))

        // 注意書きをまとめるためのColumn
        Column(
            modifier = Modifier
                .fillMaxWidth(1f) // 幅を画面の60%に設定
                .align(Alignment.End) // 親の水平中央揃えを上書きし、左に寄せる
        ) {
            Text(
                text = "※効果算出には環境省による「デコ活データベース」を利用しています",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "※効果はあくまでも概算であり、実際の値を証明するものではありません",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        // ▲▲▲▲▲ ここまで追加 ▲▲▲▲▲
    }
}

@Composable
fun ThinDivider(
    color: Color = Color.LightGray
) {
    Divider(
        color = color,
        thickness = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun StatItemWithIcon(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = bodyStyle,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                style = bodyStyle,
            )
        }
        Text(
            text = value,
            style = boldBodyStyle,
        )
        ThinDivider()
    }
}

@Composable
fun StatItemWithIconAndDifference(
    icon: String,
    label: String,
    value: String,
    difference: String,
    differenceColor: Color = Color.Gray
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = bodyStyle,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                style = bodyStyle,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = boldBodyStyle,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = difference,
                fontSize = 30.sp,
                color = differenceColor
            )
        }
        ThinDivider()
    }
}

@Preview
@Composable
fun StatisticsContentPreview() {
    // プレビューでは Hilt/ViewModel を使わずダミー表示
    StatisticsContent()
}