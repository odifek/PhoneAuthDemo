package com.sprinthubmobile.example.phoneauthdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.PhoneAuthProvider
import com.sprinthubmobile.example.phoneauthdemo.databinding.ActivityLoginBinding
import com.sprinthubmobile.example.phoneauthdemo.login.Lce
import com.sprinthubmobile.example.phoneauthdemo.login.LoginResultState
import com.sprinthubmobile.example.phoneauthdemo.login.LoginViewModel
import timber.log.Timber


class LoginActivity : AppCompatActivity() {

    lateinit var loginViewModel: LoginViewModel

    private lateinit var navController: NavController
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        navController = findNavController(R.id.login_navhost_fragment)

        val factory: LoginViewModel.Factory = LoginViewModel.Factory(Injection.loginRepository)
        loginViewModel = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)
        loginViewModel.loginResultState.observe(this, Observer {
            when (it) {
                // We only handle result here.
                is Lce.Content -> {
                    if (!it.data.isHandled) {
                        it.data.getContentIfNotHandled()?.let { resultState -> processResult(resultState) }
                    }
                }
            }
        })

        savedInstanceState?.let {
            phoneNumber = it.getString(KEY_PHONE_NUMBER)
            verificationId = it.getString(KEY_VERIFICATION_ID)
            verificationInProgress = it.getBoolean(KEY_VERIFICATION_IN_PROGRESS)
            resendingToken = it.getParcelable(KEY_RESENDING_TOKEN)

            if (verificationInProgress) {
                //continuePhoneVerification(phoneNumber)
            }
        }
    }

    private fun continuePhoneVerification(phoneNumber: String?) {
        phoneNumber?.let { loginViewModel.verifyPhone(it) }
        //navController.navigate(EnterPhoneNumberFragmentDirections.actionEnterNumberPhoneFragmentToVerificationFragment())
    }


    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationId: String? = null

    private var verificationInProgress: Boolean = false

    private var phoneNumber: String? = null

    private fun processResult(resultState: LoginResultState) {
        when (resultState) {
            is LoginResultState.CodeSent -> {
                Timber.i("Code Sent to %s", resultState.phoneNumber)
                resendingToken = resultState.resendingToken
                verificationId = resultState.verificationId
                phoneNumber = resultState.phoneNumber
                verificationInProgress = true

                navController.navigate(
                    EnterPhoneNumberFragmentDirections
                        .actionEnterNumberPhoneFragmentToVerificationFragment()
                )
            }
            is LoginResultState.LoginSuccess -> {
                Timber.i("Login success: %s", resultState.userId)
                verificationInProgress = false
                Snackbar.make(binding.constraintlayoutLogin, "Success login", Snackbar.LENGTH_SHORT)

                val mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
                //navController.navigate()
            }
            is LoginResultState.LoginFailure -> {
                Timber.w(resultState.error, "Login Failure: %s", resultState.error.message)
                verificationInProgress = false
                Snackbar.make(
                    binding.constraintlayoutLogin,
                    "Login failure: ${resultState.error.message}",
                    Snackbar.LENGTH_SHORT
                )
            }

            is LoginResultState.VerificationFailed -> {
                Snackbar.make(
                    binding.constraintlayoutLogin,
                    "Verification failure: ${resultState.error?.message}",
                    Snackbar.LENGTH_SHORT
                )
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_RESENDING_TOKEN, resendingToken)
        outState.putString(KEY_VERIFICATION_ID, verificationId)
        outState.putBoolean(KEY_VERIFICATION_IN_PROGRESS, verificationInProgress)
        outState.putString(KEY_PHONE_NUMBER, phoneNumber)
        super.onSaveInstanceState(outState)
    }


}

private const val KEY_RESENDING_TOKEN = "resendingToken"
private const val KEY_VERIFICATION_ID: String = "verificaitonId"
private const val KEY_VERIFICATION_IN_PROGRESS = "verificaitonInProgress"
private const val KEY_PHONE_NUMBER = "phoneNumber"

