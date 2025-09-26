package com.example.ecodule.ui.account.util

/**
 * メールアドレスのバリデーションを行うユーティリティクラス
 */
object EmailValidator {

    /**
     * メールアドレスの有効性を検証する
     * @param email 検証するメールアドレス
     * @return 有効な場合はtrue、無効な場合はfalse
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false // 空の場合は無効とする

        // スペースが含まれる場合は無効
        if (email.contains(" ")) return false

        // @が複数含まれる場合は無効
        if (email.count { it == '@' } != 1) return false

        // @を含まない場合は無効
        if (!email.contains("@")) return false

        val parts = email.split("@")
        if (parts.size != 2) return false

        val localPart = parts[0]
        val domainPart = parts[1]

        // ローカル部の検証
        if (localPart.isEmpty()) return false

        // 許可された文字のみをチェック（英数字、ドット、ハイフン、アンダーバー）
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_"
        if (!localPart.all { it in allowedChars }) return false

        // @の直前がドットの場合は無効
        if (localPart.endsWith(".")) return false

        // ドットが連続する場合は無効
        if (localPart.contains("..")) return false

        // ドット、ハイフン、アンダーバーで始まる場合は無効
        if (localPart.startsWith(".") || localPart.startsWith("-") || localPart.startsWith("_")) return false

        // ドメイン部の検証
        if (domainPart.isEmpty()) return false

        // ドメイン部にドットが含まれているかチェック
        if (!domainPart.contains(".")) return false

        // ドメイン部の許可された文字をチェック
        val domainAllowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-"
        if (!domainPart.all { it in domainAllowedChars }) return false

        // ドメイン部がドット、ハイフンで終わる場合は無効
        if (domainPart.endsWith(".") || domainPart.endsWith("-")) return false

        // ドメイン部がドット、ハイフンで始まる場合は無効
        if (domainPart.startsWith(".") || domainPart.startsWith("-")) return false

        // ドットが連続する場合は無効
        if (domainPart.contains("..")) return false

        return true
    }

    /**
     * アカウント作成時用のメールアドレス検証
     * 空の場合は有効とする（必須チェックは別で行う）
     */
    fun isValidEmailForSignup(email: String): Boolean {
        if (email.isBlank()) return true // 空の場合は有効とする（必須チェックは別で行う）
        return isValidEmail(email)
    }
}