package com.sprinthubmobile.example.phoneauthdemo.login

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class FirebaseAuthEndPointImp(
    private val firebaseAuth: FirebaseAuth,
    private val phoneAuthProvider: PhoneAuthProvider
) :
    FirebaseAuthEndPoint {
    override fun loginWithVerifiedCodeAndId(
        verificationId: String,
        otp: String,
        executor: ExecutorService,
        callbacks: AuthLoginCallbacks
    ) {
        val authCredential = PhoneAuthProvider.getCredential(verificationId, otp)
        login(authCredential).addOnSuccessListener(executor, OnSuccessListener { callbacks.onLoginSuccess(it.user.uid) })
            .addOnFailureListener { callbacks.onLoginFailure(it) }
    }

    override fun verifyPhoneNumber(
        phoneNumber: String,
        executor: ExecutorService,
        callbacks: PhoneVerificationCallbacks
    ) {
        phoneAuthProvider.verifyPhoneNumber(phoneNumber, 120L, TimeUnit.SECONDS,
            executor, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    login(phoneAuthCredential).addOnSuccessListener(
                        executor,
                        OnSuccessListener { result ->
                            result.user?.let { callbacks.onVerificationCompleted(it.uid) }  }
                    ).addOnFailureListener(executor, OnFailureListener { callbacks.onVerificationFailed(it) })
                }

                override fun onVerificationFailed(firebaseException: FirebaseException) {
                    callbacks.onVerificationFailed(firebaseException)
                }

                override fun onCodeSent(verificationId: String, resendingToken: PhoneAuthProvider.ForceResendingToken) {
                    callbacks.onCodeSent(verificationId, resendingToken)
                }

            })
    }

    private fun login(authCredential: AuthCredential): Task<AuthResult> {
        return firebaseAuth.signInWithCredential(authCredential)
    }

}
