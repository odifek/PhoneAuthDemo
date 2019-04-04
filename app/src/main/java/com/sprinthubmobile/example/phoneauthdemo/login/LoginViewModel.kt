package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val mutableLoginResultState: MutableLiveData<Lce<LoginResultState>> = MutableLiveData()

    val loginResultState: LiveData<Lce<LoginResultState>>
        get() = mutableLoginResultState

    fun verifyPhone(phoneNumber: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class Factory(private val loginRepository: LoginRepository) : ViewModelProvider.Factory {
        @SuppressWarnings("unchecked")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(loginRepository) as T
        }

    }
}