package com.example.ecodule.ui.account.api

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.delay

sealed class googleAuthApiResult {
    data class Success(val idToken: String) : googleAuthApiResult()
    data class Cancelled(val reason: String?) : googleAuthApiResult()
    data class NetworkError(val message: String?) : googleAuthApiResult()
    object NoCredentials : googleAuthApiResult()
    data class Error(val throwable: Throwable) : googleAuthApiResult()
}

suspend fun googleAuthApi(
    activity: Activity,
    request: GetCredentialRequest
): googleAuthApiResult {
    val cm = CredentialManager.create(activity)

    // 初回フレーム直後の誤検知を避ける軽い待ち
    delay(10)

    return try {
        Log.d("GoogleAuthApi", "Credential obtained: $activity, $request")
        Log.d("GoogleAuthApi", "$cm")
        val res = cm.getCredential(context = activity, request = request)

        val cred = res.credential
        if (cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val google = GoogleIdTokenCredential.createFrom(cred.data)
            googleAuthApiResult.Success(google.idToken)
        } else {
            googleAuthApiResult.Error(IllegalStateException("Unexpected credential type: ${cred.type}"))
        }
    } catch (e: NoCredentialException) {
        googleAuthApiResult.NoCredentials
    } catch (e: GetCredentialCancellationException) {
        if (e.localizedMessage == "[16] Account reauth failed.") {
            googleAuthApiResult.NetworkError(e.localizedMessage)
        } else {
            googleAuthApiResult.Cancelled(e.message)
        }
    } catch (e: GoogleIdTokenParsingException) {
        googleAuthApiResult.Error(e)
    } catch (e: androidx.credentials.exceptions.GetCredentialException) {
        googleAuthApiResult.Error(e)
    } catch (e: Exception) {
        googleAuthApiResult.Error(e)
    }
}