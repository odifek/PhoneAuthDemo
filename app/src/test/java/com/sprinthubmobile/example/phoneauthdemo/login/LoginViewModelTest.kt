package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.annotation.NonNull
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthProvider
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import org.hamcrest.CoreMatchers.*
import org.junit.BeforeClass
import org.junit.Rule
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {
    // region constants ----------------------------------------------------------------------------

    // endregion constants -------------------------------------------------------------------------

    // region helper fields ------------------------------------------------------------------------
    @get: Rule
    val rule = InstantTaskExecutorRule()


    private lateinit var SUT: LoginViewModel

    @Mock
    private lateinit var loginResultStateObserver: Observer<Lce<LoginResultState>>

    @Mock
    lateinit var loginRepository: LoginRepository
    @Mock
    lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken


    // endregion helper fields ---------------------------------------------------------------------

    // https://stackoverflow.com/questions/43356314/android-rxjava-2-junit-test-getmainlooper-in-android-os-looper-not-mocked-runt
    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpRxSchedulers() {

            val immediate = object : Scheduler() {
                override fun scheduleDirect(@NonNull run: Runnable, delay: Long, @NonNull unit: TimeUnit): Disposable {
                    // this prevents StackOverflowErrors when scheduling with a delay
                    return super.scheduleDirect(run, 0, unit)
                }

                override fun createWorker(): Worker {
                    return ExecutorScheduler.ExecutorWorker(Executor { it.run() })
                }
            }

            RxJavaPlugins.setInitIoSchedulerHandler { scheduler -> immediate }
            RxJavaPlugins.setInitComputationSchedulerHandler { scheduler -> immediate }
            RxJavaPlugins.setInitNewThreadSchedulerHandler { scheduler -> immediate }
            RxJavaPlugins.setInitSingleSchedulerHandler { scheduler -> immediate }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler -> immediate }
        }
    }

    @Before
    fun setUp() {
        SUT = LoginViewModel(loginRepository)


    }

    private val verificationId = "verificationId"
    private val phoneNumber = "01234567890"


    @Test
    fun verifyPhone_shouldCallRepositoryMethodWithPhoneNumber() {
        // Setup
        val codeSentObservable: Observable<LoginResultState> =
            Observable.just(LoginResultState.CodeSent(verificationId, resendingToken, ""))
        whenever(loginRepository.verifyPhoneNumber(anyString())).thenReturn(codeSentObservable)

        // Execute
        SUT.verifyPhone(phoneNumber)

        // Verify
        argumentCaptor<String> {
            verify(loginRepository).verifyPhoneNumber(capture())
            assertThat(firstValue, `is`(phoneNumber))
        }

    }

    // verifyPhone - code sent - returns resending token and verification id
    @Test
    fun verifyPhone_codeSent_shouldUpdateUiStateWithTheResultStateContainingVerificationIdAndResendToken() {
        // Setup
        val codeSentObservable: Observable<LoginResultState> =
            Observable.just(LoginResultState.CodeSent(verificationId, resendingToken, ""))
        whenever(loginRepository.verifyPhoneNumber(anyString())).thenReturn(codeSentObservable)

        val phoneVerifyLoading: Lce<LoginResultState> = Lce.Loading(true)
        val codeSentResultState: LoginResultState = LoginResultState.CodeSent(verificationId, resendingToken, "")
        val codeSentResultLce: Lce<LoginResultState> = Lce.Content(codeSentResultState)

        val resultCaptor: KArgumentCaptor<Lce<LoginResultState>> = argumentCaptor()

        SUT.loginResultState.observeForever(loginResultStateObserver)

        // Execute
        SUT.verifyPhone(phoneNumber)

        // Verify
        inOrder(loginResultStateObserver) {
            verify(loginResultStateObserver).onChanged(phoneVerifyLoading)
            verify(loginResultStateObserver).onChanged(resultCaptor.capture())

            val result: Lce<LoginResultState> = resultCaptor.firstValue
            assertThat(
                result is Lce.Content && result.data
                        is LoginResultState.CodeSent && (result.data
                        as LoginResultState.CodeSent).verificationId == verificationId, `is`(true)
            )
        }

    }

    @Mock
    private lateinit var authResultMock: AuthResult

    private val userId = "userId"

    // verifyPhone - verificationCompleted - should perform login and return auth result
    @Test
    fun verifyPhone_autoVerification_shouldUpdateUiStateWithTheResultStateIndicatingSuccessLogin() {
        // Setup
        val autoVerifySequenceObservable: Observable<LoginResultState> =
            Observable.just(
                LoginResultState.CodeSent(verificationId, resendingToken, ""),
                LoginResultState.LoginSuccess(userId)
            )
        whenever(loginRepository.verifyPhoneNumber(anyString())).thenReturn(autoVerifySequenceObservable)

        val phoneVerifyLoading: Lce<LoginResultState> = Lce.Loading(true)
        val codeSentResultState: LoginResultState = LoginResultState.CodeSent(verificationId, resendingToken, "")
        val codeSentResultLce: Lce<LoginResultState> = Lce.Content(codeSentResultState)

        val resultCaptor: KArgumentCaptor<Lce<LoginResultState>> = argumentCaptor()

        SUT.loginResultState.observeForever(loginResultStateObserver)

        // Execute
        SUT.verifyPhone(phoneNumber)

        // Verify
        inOrder(loginResultStateObserver) {
            verify(loginResultStateObserver).onChanged(phoneVerifyLoading)
            verify(loginResultStateObserver, times(2)).onChanged(resultCaptor.capture())

            val codeSentResult: Lce<LoginResultState> = resultCaptor.firstValue
            val loginSuccessResult : Lce<LoginResultState> = resultCaptor.secondValue
            assertThat(codeSentResult is Lce.Content && codeSentResult.data is LoginResultState.CodeSent, `is`(true))
            assertThat(loginSuccessResult is Lce.Content && loginSuccessResult.data is LoginResultState.LoginSuccess, `is`(true))
        }

    }

    @Mock
    private lateinit var firebaseException: FirebaseException

    // verifyPhone - failure - should return failure reason and exception
    @Test
    fun verifyPhone_verificationFailed_shouldUpdateUiStateWithTheResultStateIndicatingVerificaitonFailure() {
        // Setup
        val errorMessage = "Invalid Request"
        val verificationError = Throwable(errorMessage, firebaseException)
        val verifyFailureObservable: Observable<LoginResultState> =
            Observable.just(
                LoginResultState.VerificationFailed(verificationError, errorMessage, AUTH_INVALID_REQUEST)
            )
        whenever(loginRepository.verifyPhoneNumber(anyString())).thenReturn(verifyFailureObservable)

        val phoneVerifyLoading: Lce<LoginResultState> = Lce.Loading(true)
        val resultCaptor: KArgumentCaptor<Lce<LoginResultState>> = argumentCaptor()

        SUT.loginResultState.observeForever(loginResultStateObserver)

        // Execute
        SUT.verifyPhone(phoneNumber)

        // Verify
        inOrder(loginResultStateObserver) {
            verify(loginResultStateObserver).onChanged(phoneVerifyLoading)
            verify(loginResultStateObserver, times(1)).onChanged(resultCaptor.capture())

            val verificationFailureResult : Lce<LoginResultState> = resultCaptor.firstValue
            assertThat(verificationFailureResult is Lce.Content && verificationFailureResult.data is LoginResultState.VerificationFailed, `is`(true))
        }

    }



    // region test cases ---------------------------------------------------------------------------

    // endregion test cases ------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    // endregion helper methods --------------------------------------------------------------------

    // region helper classes -----------------------------------------------------------------------

    // end region helper class ---------------------------------------------------------------------
}