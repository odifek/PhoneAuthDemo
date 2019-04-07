package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val mutableLoginResultState: MutableLiveData<Lce<LoginResultState>> = MutableLiveData()

    val loginResultState: LiveData<Lce<LoginResultState>>
        get() = mutableLoginResultState

    private val disposables = CompositeDisposable()

    fun verifyPhone(phoneNumber: String) {
        val disposable = loginRepository.verifyPhoneNumber(phoneNumber)
            .compose(stateToLce())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ mutableLoginResultState.value = it },
                { throwable: Throwable ->
                    Timber.w(throwable, "Error starting")
                    mutableLoginResultState.value = Lce.Error(throwable)
                })

        disposables.add(disposable)
    }

    private fun stateToLce() = ObservableTransformer<LoginResultState, Lce<LoginResultState>> { upstream ->
        upstream.map { result ->
            val lce: Lce<LoginResultState> = Lce.Content(result)
            lce
        }.startWith(Lce.Loading(true))
    }

    class Factory(private val loginRepository: LoginRepository) : ViewModelProvider.Factory {
        @SuppressWarnings("unchecked")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(loginRepository) as T
        }

    }
}