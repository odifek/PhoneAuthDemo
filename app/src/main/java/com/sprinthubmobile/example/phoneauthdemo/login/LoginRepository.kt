package com.sprinthubmobile.example.phoneauthdemo.login

import io.reactivex.Observable

interface LoginRepository {
    fun verifyPhoneNumber(phoneNumber: String): Observable<LoginResultState>
    fun loginWithOtp(verificationId: String, otp: String): Observable<LoginResultState>

}
