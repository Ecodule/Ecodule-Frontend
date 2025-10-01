package com.example.ecodule.ui.settings.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar


val currentYear = Calendar.getInstance().get(Calendar.YEAR)
val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

@Composable
fun WheelsDatePickerDialog(
    label: String,
    currentBirthDate: String,
    onSelectedDateChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onBirthDateChanged: (String) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        DatePickerUI(
            label = label,
//            onSelectedDateChange = onSelectedDateChange,
            currentBirthDate = currentBirthDate,
            onDismissRequest = onDismissRequest,
            onBirthDateChanged = onBirthDateChanged,
        )
    }
}

@Composable
fun DatePickerUI(
    label: String,
    currentBirthDate: String,
//    onSelectedDateChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onBirthDateChanged: (String) -> Unit = {}
) {
    var birthDate by remember { mutableStateOf(currentBirthDate) }//これであってる？

    Card(
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 5.dp)
        ) {
            // ダイアログのラベル
            Text(
                text = label,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            val chosenYear = remember { mutableStateOf(currentYear) }
            val chosenMonth = remember { mutableStateOf(currentMonth) }
            val chosenDay = remember { mutableStateOf(currentDay) }

            val daysInMonth = remember(chosenYear.value, chosenMonth.value) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, chosenYear.value)
                calendar.set(Calendar.MONTH, chosenMonth.value - 1) // Calendarクラスでは月が0から始まるので1を引く
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            val daysList = remember(chosenYear.value, chosenMonth.value) {
                (1..daysInMonth).map { it.toString() }
            }

            // 日付選択領域
            DateSelectionSection(
                onYearSelected = { chosenYear.value = it.toInt() },
                onMonthSelected = { chosenMonth.value = it.toInt() },
                days = daysList,
                onDaySelected = { chosenDay.value = it.toInt() },
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 確定ボタン
            Button(
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    birthDate =
                        "${chosenYear.value}/${chosenMonth.value}/${chosenDay.value}"
                    onBirthDateChanged(birthDate)
//                  onSelectedDateChange(BirthDate)
                    onDismissRequest()

                }
            ) {
                Text(
                    text = "確定",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ItemsPicker(
    items: List<String>,
    firstIndex: Int,
    onItemSelected: (String) -> Unit,
) {
    val listState = rememberLazyListState(firstIndex)
    val currentValue = remember { mutableStateOf(items.getOrElse(firstIndex) { "" }) }

    LaunchedEffect(key1 = listState.isScrollInProgress) {
        val firstVisibleItemIndex = listState.firstVisibleItemIndex
        currentValue.value = items.getOrElse(firstVisibleItemIndex + 1) { "" }
        onItemSelected(currentValue.value)
        // 選択した数字を上下中央に自動スクロールさせる
        listState.animateScrollToItem(index = firstVisibleItemIndex)
    }

    Box(modifier = Modifier.height(106.dp)) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState,
            content = {
                items(count = items.size) { index ->
                    // 状態が変更されたときのみ透明度を再計算するためderivedStateOfを使う
                    val alpha by remember {
                        derivedStateOf {
                            when (index) {
                                0, items.lastIndex -> 0f // 余白用に追加したtopとbottom
                                listState.firstVisibleItemIndex + 1 -> 1f // 選択中
                                else -> 0.3f
                            }
                        }
                    }
                    Text(
                        text = items[index],
                        modifier = Modifier.alpha(alpha),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        )
    }
}