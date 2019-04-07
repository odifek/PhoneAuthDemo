package com.sprinthubmobile.example.phoneauthdemo.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.Executor

class FirebaseEndpoint(
    private val firebaseAuth: FirebaseAuth,
    private val phoneAuthProvider: PhoneAuthProvider,
    val executor: Executor
) {
    fun verifyPhoneNumber(phoneNumber: String, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {

    }

}
