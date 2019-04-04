package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginFeatureTest {

    @get: Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var loginRepository: LoginRepository

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    lateinit var firebaseUser: FirebaseUser

    @Mock
    private lateinit var loginResultStateObserver: Observer<Lce<LoginResultState>>

    lateinit var subject: LoginViewModel

    @Before
    fun setUp() {
        loginRepository = FirebaseLoginRepository(firebaseAuth)
        subject = LoginViewModel(loginRepository)
    }


    @Test
    fun verifyPhoneNumber_success_shouldReturnCodeSentAndVerificationId() {
        // Setup
        val phoneNumber = "012345678901"
        subject.loginResultState.observeForever(loginResultStateObserver)

        // Execute
        subject.verifyPhone(phoneNumber)

        // Verify
        val loadingState: Lce<LoginResultState> = Lce.Loading(true)
        verify(loginResultStateObserver).onChanged(loadingState)
        val successCodeSent = LoginResultState()
        val codeSentState: Lce<LoginResultState> = Lce.Content(successCodeSent)
        verify(loginResultStateObserver).onChanged(codeSentState)

    }
}