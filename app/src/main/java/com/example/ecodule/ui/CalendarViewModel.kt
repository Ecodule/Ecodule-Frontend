/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ecodule.ui

import androidx.lifecycle.ViewModel

// UIで使うデータを準備し、保持するビューモデル
// UI の状態（例：読み込み中、エラー、表示データなど）を管理し、UI に公開します。多くの場合、StateFlow や LiveData を使って UI に状態を通知します。
class CalendarViewModel : ViewModel() {

}

//data class ReplyHomeUIState(
//    val emails: List<Email> = emptyList(),
//    val selectedEmail: Email? = null,
//    val isDetailOnlyOpen: Boolean = false,
//    val loading: Boolean = false,
//    val error: String? = null
//)
