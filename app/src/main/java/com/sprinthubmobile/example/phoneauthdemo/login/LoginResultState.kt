package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.annotation.IntDef
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthProvider

const val AUTH_INVALID_REQUEST = 10
const val AUTH_TOO_MANY_REQUESTS = 11
const val AUTH_RECENT_LOGIN_REQUIRED = 13
const val AUTH_CODE_EXPIRED = 14
const val AUTH_INVALID_CODE = 15

const val AUTH_OTHER_ERROR = 21
sealed class LoginResultState {

    data class CodeSent(
        val verificationId: String?,
        val resendingToken: PhoneAuthProvider.ForceResendingToken?,
        val phoneNumber: String
    ) : LoginResultState()

    data class LoginSuccess(val userId: String) : LoginResultState()
    data class VerificationFailed(val error: Throwable?, val message: String, @VerificationError val errorCode: Int) : LoginResultState()
    data class LoginFailure(val error: Throwable, @LoginError val errorCode: Int) : LoginResultState()


    @IntDef(AUTH_CODE_EXPIRED, AUTH_INVALID_REQUEST, AUTH_TOO_MANY_REQUESTS, AUTH_OTHER_ERROR)
    @Retention(AnnotationRetention.SOURCE)
    annotation class VerificationError

    @IntDef(AUTH_CODE_EXPIRED, AUTH_INVALID_CODE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LoginError

}
