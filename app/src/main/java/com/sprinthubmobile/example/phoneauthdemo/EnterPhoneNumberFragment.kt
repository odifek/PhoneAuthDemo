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
import com.sprinthubmobile.example.phoneauthdemo.databinding.FragmentEnterPhoneNumberBinding
import com.sprinthubmobile.example.phoneauthdemo.login.Lce
import com.sprinthubmobile.example.phoneauthdemo.login.LoginResultState
import com.sprinthubmobile.example.phoneauthdemo.login.LoginViewModel


class EnterPhoneNumberFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory: LoginViewModel.Factory = LoginViewModel.Factory(Injection.loginRepository)
        loginViewModel = ViewModelProviders.of(activity!!, factory).get(LoginViewModel::class.java)
    }

    private lateinit var binding: FragmentEnterPhoneNumberBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_phone_number, container, false)

        binding.textinputedittextPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length >= 10 && !binding.buttonEnterPhoneNumberContinue.isEnabled) {
                        binding.buttonEnterPhoneNumberContinue.isEnabled = true
                    } else if (binding.buttonEnterPhoneNumberContinue.isEnabled) {
                        binding.buttonEnterPhoneNumberContinue.isEnabled = false
                    }
                }
            }

        })
        binding.buttonEnterPhoneNumberContinue.setOnClickListener {
            val phoneNumber = binding.textinputedittextPhoneNumber.text?.toString()
            phoneNumber?.let { loginViewModel.verifyPhone(it) }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginViewModel.loginResultState.observe(this, Observer {
            when (it) {
                is Lce.Loading -> {
                    showUiLoading(true)
                }
                is Lce.Error -> {
                    showUiLoading(false, it.throwable.message)
                }
                is Lce.Content -> {
                    val result = it.data
                    result.getContentIfNotHandled()?.let { it1 -> processResult(it1) }
                }
            }
        })
    }

    private fun processResult(result: LoginResultState) {
        when (result) {
            is LoginResultState.VerificationFailed -> showUiLoading(false, result.error?.message)
            is LoginResultState.LoginFailure -> showUiLoading(false, result.error.message)
        }
    }

    private fun showUiLoading(loading: Boolean, errorMessage: String? = null) {
        if (loading) {
            binding.progressbarPhoneVerifying.visibility = View.VISIBLE
        } else {
            binding.progressbarPhoneVerifying.visibility = View.GONE
        }

        binding.buttonEnterPhoneNumberContinue.isEnabled = !loading
        binding.textinputlayoutPhoneNumber.error = errorMessage
    }
}
