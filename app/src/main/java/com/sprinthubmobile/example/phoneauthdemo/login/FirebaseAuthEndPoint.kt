package com.sprinthubmobile.example.phoneauthdemo.login

import java.util.concurrent.ExecutorService

interface FirebaseAuthEndPoint {
    fun verifyPhoneNumber(
        phoneNumber: String,
        executor: ExecutorService,
        callbacks: PhoneVerificationCallbacks
    )

    fun loginWithVerifiedCodeAndId(
        verificationId: String,
        otp: String,
        executor: ExecutorService,
        callbacks: AuthLoginCallbacks
    )

}
