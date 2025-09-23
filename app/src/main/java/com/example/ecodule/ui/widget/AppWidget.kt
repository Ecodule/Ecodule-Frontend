package com.example.ecodule.ui.widget

import androidx.glance.appwidget.GlanceAppWidget
import android.content.Context
import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
//import androidx.glance.LocalContext
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
//import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
//import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
//import com.example.ecodule.R
import java.time.LocalDate

class AppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent(
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.background)
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    @Composable
    private fun MyContent(modifier: GlanceModifier = GlanceModifier) {
        //Composable形式でレイアウトを記述*Glanceのcomposableを使用すること
        // composeとglanceは一緒に書けないので注意
//        Box(
//            modifier = modifier
//                .fillMaxSize()
//                .background(GlanceTheme.colors.surface),
//            contentAlignment = Alignment.Center,
//        ) {
//            Text(
//                text = (LocalContext.current.getString(R.string.widget_description)),
//                style = TextStyle(
//                    color = GlanceTheme.colors.onSurface,)
//            )
//        }

        Column(
            modifier
                .background(GlanceTheme.colors.surface)
                .fillMaxSize()
                .padding(8.dp)
        ) {
                ProvideSchedule()
            /*
            getSampleSchedules().forEach { schedule ->
                Text("📅 ${schedule.title} (${schedule.date})", style = TextStyle(fontWeight = FontWeight.Bold))
                schedule.tasks.forEach { task ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CheckBox(
                            checked = task.isDone,
                            onCheckedChange = null // テスト用
                        )
                        Text(task.title)
                    }
                }
                Spacer(GlanceModifier.height(8.dp))
            }*/
        }


    }

    data class Task(
        val id: Int,
        val title: String,
        val isDone: Boolean
    )

    data class Schedule(
        val id: Int,
        val title: String,
        val date: LocalDate,
        val tasks: List<Task>
    )
/*
    @Composable
    private fun getSampleSchedules(
    ): List<Schedule> {
        return listOf(
            Schedule(
                id = 1,
                title = "研究発表準備",
                date = LocalDate.of(2025, 9, 15),
                tasks = listOf(
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true)
                )
            ),
            Schedule(
                id = 2,
                title = "週末の買い出し",
                date = LocalDate.of(2025, 9, 16),
                tasks = listOf(
                    Task(4, "スーパーで食材購入", true),
                    Task(5, "ドラッグストアで日用品", false)
                )
            )
        )
    }*/


    private fun getSchedule():List<Schedule> {//サーバから予定を取得する
        return listOf(//get()でスケジュールを取得　もしかしたらスケジュール全部を取得、リストに時間順でまとめて最初の予定をprovideに渡すかも？.
            // 　以下はスケジュールのテスト用データ1
            Schedule(
                id = 1,
                title = "研究発表準備",
                date = LocalDate.of(2025, 9, 15),
                tasks = listOf(
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true),
                    Task(1, "スライド作成", false),
                    Task(2, "発表練習", false),
                    Task(3, "資料印刷", true)
                )
            )
        )

    }

    @Composable
    private fun ProvideSchedule(

    ){//ウィジェットに予定とタスクを表示する
        getSchedule().forEach{ schedule ->
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.background)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ){
                Row(modifier = GlanceModifier
                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                ){
                    Text(schedule.title + schedule.date, style = TextStyle(fontWeight = FontWeight.Bold))
                    Spacer(GlanceModifier.width(8.dp))
                    Button(
                        text = "完了",
                        onClick = {}
                    )

                }

                LazyColumn {
                    items(schedule.tasks) { task ->
                        Row(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CheckBox(
                                checked = task.isDone,
                                onCheckedChange = null//{update(it.id)}
                            )
                            Text(task.title)
                        }
                    }
                }
            }



        }
    }

}