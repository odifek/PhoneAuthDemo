package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.annotation.NonNull
import com.google.firebase.auth.*
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import org.junit.BeforeClass
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class FirebaseLoginRepositoryTest {
    // region constants ----------------------------------------------------------------------------
    private val phoneNumber = "01234567890"
    private val verificationId = "verificationId"
    // endregion constants -------------------------------------------------------------------------

    // region helper fields ------------------------------------------------------------------------
    private lateinit var SUT: FirebaseLoginRepository

    @Mock
    lateinit var firebaseAuth: FirebaseAuth
    @Mock
    lateinit var phoneAuthProvider: PhoneAuthProvider
    @Mock
    lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken

    lateinit var executor: ExecutorService

    @Mock
    lateinit var firebaseAuthEndpoint: FirebaseAuthEndPoint

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
        executor = Executors.newSingleThreadExecutor()
        //firebaseAuthEndpoint = FirebaseEndpoint(firebaseAuth, phoneAuthProvider, executor)
        SUT = FirebaseLoginRepository(firebaseAuthEndpoint, executor)

    }

    @Test
    fun verifyPhoneNumber_shouldCallFirebaseAuthEndpointWithCorrectValues() {
        // Setup

        val phoneCaptor: KArgumentCaptor<String> = argumentCaptor()
        val executorCaptor: KArgumentCaptor<ExecutorService> = argumentCaptor()
        val loginTestObserver: TestObserver<LoginResultState> = TestObserver.create()

        // Execute
        SUT.verifyPhoneNumber(phoneNumber).subscribe(loginTestObserver)
        loginTestObserver.awaitTerminalEvent(2, TimeUnit.SECONDS)

        // Verify
        argumentCaptor<PhoneVerificationCallbacks> {
            verify(firebaseAuthEndpoint).verifyPhoneNumber(phoneCaptor.capture(), executorCaptor.capture(), capture())
            val callback = firstValue

            assertThat(phoneCaptor.firstValue, `is`(phoneNumber))
            assertThat(executorCaptor.firstValue, `is`(executor))
        }

    }

    @Test
    fun verifyPhoneNumber_codeSent_shouldReturnLoginStateAlongWithTheVerificationIdAndResendingToken() {
        // Setup
        val phoneCaptor: KArgumentCaptor<String> = argumentCaptor()
        val executorCaptor: KArgumentCaptor<ExecutorService> = argumentCaptor()
        val loginTestObserver: TestObserver<LoginResultState> = TestObserver.create()

        val codeSentState: LoginResultState = LoginResultState.CodeSent(
            verificationId,
            resendingToken,
            ""
        )

        // Execute
        SUT.verifyPhoneNumber(phoneNumber).subscribe(loginTestObserver)
        loginTestObserver.awaitTerminalEvent(2, TimeUnit.SECONDS)

        // Verify
        argumentCaptor<PhoneVerificationCallbacks> {
            verify(firebaseAuthEndpoint).verifyPhoneNumber(phoneCaptor.capture(), executorCaptor.capture(), capture())
            val callback = firstValue
            callback.onCodeSent(verificationId, resendingToken)
        }
        loginTestObserver.assertNoErrors()
        loginTestObserver.assertValue(codeSentState)
    }

    @Mock
    private lateinit var phoneAuthCredential: AuthCredential

    @Mock
    private lateinit var authResult: AuthResult

    @Test
    fun verifyPhoneNumber_verificationCompleted_shouldReturnLoginResultStateWithSuccessSignIn() {
        // Setup
        val phoneCaptor: KArgumentCaptor<String> = argumentCaptor()
        val executorCaptor: KArgumentCaptor<ExecutorService> = argumentCaptor()
        val loginTestObserver: TestObserver<LoginResultState> = TestObserver.create()

        val loginSuccessfulState: LoginResultState =
            LoginResultState.LoginSuccess(userId)

        // Execute
        SUT.verifyPhoneNumber(phoneNumber).subscribe(loginTestObserver)
        loginTestObserver.awaitTerminalEvent(2, TimeUnit.SECONDS)

        // Verify
        argumentCaptor<PhoneVerificationCallbacks> {
            verify(firebaseAuthEndpoint).verifyPhoneNumber(phoneCaptor.capture(), executorCaptor.capture(), capture())
            val callback = firstValue
            callback.onVerificationCompleted(userId)
        }
        loginTestObserver.assertNoErrors()
        loginTestObserver.assertValue(loginSuccessfulState)
    }

    @Mock
    private lateinit var invalidCredentialsException: FirebaseAuthInvalidCredentialsException

    @Test
    fun verifyPhoneNumber_verificationFailedWithInvalidRequest_shouldReturnFailureStateWithErrorMessageShowingUserEnteredPhoneNumberIsIncorrect() {
        // Setup
        val phoneCaptor: KArgumentCaptor<String> = argumentCaptor()
        val executorCaptor: KArgumentCaptor<ExecutorService> = argumentCaptor()
        val loginTestObserver: TestObserver<LoginResultState> = TestObserver.create()

        val errorMessage = "Invalid phone format"
        val invalidRequestError = Throwable(errorMessage)
        val verificationFailureState: LoginResultState =
            LoginResultState.VerificationFailed(invalidRequestError, errorMessage, AUTH_INVALID_REQUEST)

        whenever(invalidCredentialsException.message).thenReturn(errorMessage)

        // Execute
        SUT.verifyPhoneNumber(phoneNumber).subscribe(loginTestObserver)
        loginTestObserver.awaitTerminalEvent(2, TimeUnit.SECONDS)

        // Verify
        argumentCaptor<PhoneVerificationCallbacks> {
            verify(firebaseAuthEndpoint).verifyPhoneNumber(phoneCaptor.capture(), executorCaptor.capture(), capture())
            val callback = firstValue
            callback.onVerificationFailed(invalidCredentialsException)
        }
        loginTestObserver.assertNoErrors()
        loginTestObserver.assertValue { resState: LoginResultState ->
            resState is LoginResultState.VerificationFailed && resState.message == errorMessage
        }
    }

    private val otp = "123456"

    private val userId = "userId"

    @Test
    fun loginWithVerificationIdAndOtp_success_returnsSuccessLoginResultState() {

        val executorCaptor: KArgumentCaptor<ExecutorService> = argumentCaptor()
        val loginArgCaptor: KArgumentCaptor<String> = argumentCaptor()
        val loginTestObserver: TestObserver<LoginResultState> = TestObserver.create()

        val loginSuccessfulState: LoginResultState =
            LoginResultState.LoginSuccess(userId)

        // Execute
        SUT.loginWithOtp(verificationId, otp).subscribe(loginTestObserver)
        loginTestObserver.await(2, TimeUnit.SECONDS)

        // Verify
        argumentCaptor<AuthLoginCallbacks> {
            verify(firebaseAuthEndpoint).loginWithVerifiedCodeAndId(
                loginArgCaptor.capture(), loginArgCaptor.capture(),
                executorCaptor.capture(), capture()
            )

            val callback = firstValue
            callback.onLoginSuccess(userId)

            assertThat(loginArgCaptor.firstValue, `is`(verificationId))
            assertThat(loginArgCaptor.secondValue, `is`(otp))
        }

        loginTestObserver.assertNoErrors()
        loginTestObserver.assertValue(loginSuccessfulState)
    }

    @Mock
    private lateinit var firebaseInvalidCredentialException: FirebaseAuthInvalidCredentialsException

    @Test
    fun loginWithVerificationIdAndOtp_invalidCode_returnsFailureLoginResultState() {

        val executorCaptor: KArgumentCaptor<ExecutorService> = argumentCaptor()
        val loginArgCaptor: KArgumentCaptor<String> = argumentCaptor()
        val loginTestObserver: TestObserver<LoginResultState> = TestObserver.create()

        val throwable = Throwable("invalid code", firebaseInvalidCredentialException)
        val loginFailureState: LoginResultState =
            LoginResultState.LoginFailure(throwable, AUTH_INVALID_CODE)

        // Execute
        SUT.loginWithOtp(verificationId, otp).subscribe(loginTestObserver)
        loginTestObserver.await(2, TimeUnit.SECONDS)

        // Verify
        argumentCaptor<AuthLoginCallbacks> {
            verify(firebaseAuthEndpoint).loginWithVerifiedCodeAndId(
                loginArgCaptor.capture(), loginArgCaptor.capture(),
                executorCaptor.capture(), capture()
            )

            val callback = firstValue
            callback.onLoginFailure(firebaseInvalidCredentialException)

            assertThat(loginArgCaptor.firstValue, `is`(verificationId))
            assertThat(loginArgCaptor.secondValue, `is`(otp))
        }

        loginTestObserver.assertNoErrors()
        loginTestObserver.assertValue { resState ->
            resState is LoginResultState.LoginFailure
                    && resState.errorCode == AUTH_INVALID_CODE
                    && resState.error.message == throwable.message
        }
    }

    // region test cases ---------------------------------------------------------------------------

    // endregion test cases ------------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    // endregion helper methods --------------------------------------------------------------------

    // region helper classes -----------------------------------------------------------------------

    // end region helper class ---------------------------------------------------------------------
}
