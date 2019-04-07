package com.sprinthubmobile.example.phoneauthdemo.login

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.Observable
import java.lang.Exception
import java.util.concurrent.ExecutorService

class FirebaseLoginRepository(
    private val firebaseEndpoint: FirebaseAuthEndPoint,
    private val executor: ExecutorService
) : LoginRepository {
    override fun loginWithOtp(verificationId: String, otp: String): Observable<LoginResultState> {
        return Observable.create { emitter ->
            firebaseEndpoint.loginWithVerifiedCodeAndId(verificationId, otp, executor, object: AuthLoginCallbacks {
                override fun onLoginFailure(throwable: Throwable) {
                    when (throwable) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            val loginResult =
                                LoginResultState.LoginFailure(Throwable("invalid code", throwable), AUTH_INVALID_CODE)
                            emitter.onNext(loginResult)
                        }
                    }
                }

                override fun onLoginSuccess(userId: String) {
                    val loginResult = LoginResultState.LoginSuccess(userId)
                    emitter.onNext(loginResult)
                }

            })

            emitter.setCancellable { executor.shutdown() }
        }
    }

    override fun verifyPhoneNumber(phoneNumber: String): Observable<LoginResultState> {
        return Observable.create { emitter ->
            firebaseEndpoint.verifyPhoneNumber(
                phoneNumber,
                executor,
                object : PhoneVerificationCallbacks {
                    override fun onVerificationCompleted(loggedInUserId: String) {
                        val loginResult = LoginResultState.LoginSuccess(loggedInUserId)
                        emitter.onNext(loginResult)
                    }

                    override fun onVerificationFailed(firebaseException: Exception) {
                        val error = Throwable(firebaseException.message, firebaseException)
                        when (firebaseException) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                val verifyErrorState =
                                    LoginResultState.VerificationFailed(error, firebaseException.message ?: "Invalid Request", AUTH_INVALID_REQUEST)
                                emitter.onNext(verifyErrorState)
                            }
                        }
                    }

                    override fun onCodeSent(
                        verificationId: String?,
                        resendingToken: PhoneAuthProvider.ForceResendingToken?
                    ) {
                        val codeSentState = LoginResultState.CodeSent(verificationId, resendingToken, phoneNumber)
                        emitter.onNext(codeSentState)
                    }


                }
            )
            emitter.setCancellable { executor.shutdown() }
        }
    }

}

interface PhoneVerificationCallbacks {
    fun onVerificationCompleted(loggedInUserId: String)
    fun onVerificationFailed(firebaseException: Exception)
    fun onCodeSent(verificationId: String?, resendingToken: PhoneAuthProvider.ForceResendingToken?)
}

interface AuthLoginCallbacks {
    fun onLoginSuccess(userId: String)
    fun onLoginFailure(throwable: Throwable)

}