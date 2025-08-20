package com.example.ecodule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "統計",
            fontSize = 50.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        StatItemWithIcon(
            icon = "🌱",
            label = "CO₂削減量",
            value = "1.23 Kg"
        )

        StatItemWithIconAndDifference(
            icon = "💴",
            label = "今月の節約額",
            value = "12,345 円",
            difference = "-5,500",
            differenceColor = Color.Blue
        )

        StatItemWithIcon(
            icon = "🍎",
            label = "集めたりんごの数",
            value = "26 個"
        )
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
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                fontSize = 38.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = value,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
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
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = label,
                fontSize = 38.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
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
    StatisticsContent()
}
