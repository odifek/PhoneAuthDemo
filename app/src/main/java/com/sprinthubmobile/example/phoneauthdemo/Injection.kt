package com.sprinthubmobile.example.phoneauthdemo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.sprinthubmobile.example.phoneauthdemo.login.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Injection {

    val loginRepository: LoginRepository by lazy<LoginRepository> {
        FirebaseLoginRepository(firebaseEndpoint, executor)
    }

    val executor: ExecutorService by lazy<ExecutorService> { Executors.newSingleThreadExecutor() }

    val firebaseEndpoint: FirebaseAuthEndPoint by lazy<FirebaseAuthEndPoint> {
        FirebaseAuthEndPointImp(FirebaseAuth.getInstance(), PhoneAuthProvider.getInstance())
    }
}