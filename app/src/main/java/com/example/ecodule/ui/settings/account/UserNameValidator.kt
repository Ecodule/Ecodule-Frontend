package com.example.ecodule.ui.settings.account

object UserNameValidator {
    // 許可されている文字: 英数字 + . / - _
    private val allowedRegex = Regex("^[a-zA-Z0-9._/-]+$")

    fun validate(userName: String): Boolean {
        return userName.isNotBlank() && allowedRegex.matches(userName)
    }
}