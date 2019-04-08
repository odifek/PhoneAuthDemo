package com.sprinthubmobile.example.phoneauthdemo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sprinthubmobile.example.phoneauthdemo.databinding.FragmentVerificationCodeBinding
import com.sprinthubmobile.example.phoneauthdemo.login.Lce
import com.sprinthubmobile.example.phoneauthdemo.login.LoginResultState
import com.sprinthubmobile.example.phoneauthdemo.login.LoginViewModel
import timber.log.Timber

class VerificationFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate: vId: %s", verificationId)
//        savedInstanceState?.let {
//            verificationId = it.getString(VERIFICATION_ID)
//        }
        val factory: LoginViewModel.Factory = LoginViewModel.Factory(Injection.loginRepository)
        loginViewModel = ViewModelProviders.of(activity!!, factory).get(LoginViewModel::class.java)
    }

    private lateinit var binding: FragmentVerificationCodeBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_verification_code, container, false)

        binding.textinputedittextVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length == 6 && !binding.buttonVerificationCodeVerify.isEnabled) {
                        binding.buttonVerificationCodeVerify.isEnabled = true
                    } else if (binding.buttonVerificationCodeVerify.isEnabled) {
                        binding.buttonVerificationCodeVerify.isEnabled = false
                    }
                }
            }
        })

        binding.buttonVerificationCodeVerify.setOnClickListener {
            val otp = binding.textinputedittextVerificationCode.text?.toString()
            otp?.let { code ->
                verificationId?.let { vId -> loginViewModel.loginWithOtp(vId, code) }
            }
            if (verificationId == null) {
                Timber.w("We don't have the verificaton Id")
            }
        }
        return binding.root
    }

    private var verificationId: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.i("onActivityCreated")
        loginViewModel.loginResultState.observe(this, Observer {
            when (it) {
                is Lce.Loading -> showUiLoading(true)
                is Lce.Content -> {
                    processResult(it.data.peekContent())
                }
                is Lce.Error -> showUiLoading(false, it.throwable.message)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.i("onSaveInstanceState")
        //outState.putString(VERIFICATION_ID, verificationId)
        super.onSaveInstanceState(outState)
    }

    private fun processResult(result: LoginResultState) {
        when (result) {
            is LoginResultState.VerificationFailed -> showUiLoading(false, result.error?.message)
            is LoginResultState.LoginFailure -> showUiLoading(false, result.error.message)
            is LoginResultState.CodeSent -> {
                verificationId = result.verificationId
                Timber.i("Updating verification id: %s", verificationId)
            }
        }
    }

    private fun showUiLoading(loading: Boolean, errorMessage: String? = null) {
        errorMessage?.let { Timber.w("Some error occurred: %s", it) }
        if (loading) {
            binding.progressbarOtpVerify.visibility = View.VISIBLE
        } else {
            binding.progressbarOtpVerify.visibility = View.GONE
        }

        binding.buttonVerificationCodeVerify.isEnabled = !loading
        binding.textinputlayoutVerificationCode.error = errorMessage
    }
}

private const val VERIFICATION_ID = "verificationId"