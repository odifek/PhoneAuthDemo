package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.annotation.NonNull
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class LoginFeatureTest {

    @get: Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var loginRepository: LoginRepository

    @Mock private lateinit var firebaseAuth: FirebaseAuth
    @Mock private lateinit var firebaseUser: FirebaseUser
    @Mock private lateinit var phoneAuthProvider: PhoneAuthProvider
    @Mock lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    lateinit var executor: ExecutorService

    @Mock
    lateinit var firebaseAuthEndpoint: FirebaseAuthEndPoint

    @Mock
    private lateinit var loginResultStateObserver: Observer<Lce<LoginResultState>>

    lateinit var subject: LoginViewModel

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
        loginRepository = FirebaseLoginRepository(firebaseAuthEndpoint, executor)
        subject = LoginViewModel(loginRepository)
    }


    @Test
    fun verifyPhoneNumber_success_shouldReturnCodeSentAndVerificationId() {
        // Setup
        val phoneNumber = "012345678901"
        val verificationId = "verificationId"
        val successCodeSent = LoginResultState.CodeSent(verificationId, resendingToken, "")
        val codeSentState: Lce<LoginResultState> = Lce.Content(successCodeSent)
        val loadingState: Lce<LoginResultState> = Lce.Loading(true)

        subject.loginResultState.observeForever(loginResultStateObserver)

        // Execute
        subject.verifyPhone(phoneNumber)

        // Verify
        val inOrder = inOrder(loginResultStateObserver)
        inOrder.verify(loginResultStateObserver).onChanged(loadingState)
        inOrder.verify(loginResultStateObserver).onChanged(codeSentState)

    }
}