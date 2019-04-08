package com.sprinthubmobile.example.phoneauthdemo.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sprinthubmobile.example.phoneauthdemo.Event
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val mutableLoginResultState: MutableLiveData<Lce<Event<LoginResultState>>> = MutableLiveData()

    val loginResultState: LiveData<Lce<Event<LoginResultState>>>
        get() = mutableLoginResultState

    private val disposables = CompositeDisposable()

    fun verifyPhone(phoneNumber: String) {
        loginRepository.verifyPhoneNumber(phoneNumber)
            .compose(stateToLce())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ mutableLoginResultState.value = it },
                { throwable: Throwable ->
                    Timber.w(throwable, "Error starting")
                    mutableLoginResultState.value = Lce.Error(throwable)
                })
            .run { disposables.add(this) }

    }

    fun loginWithOtp(verificationId: String, otp: String) {
        loginRepository.loginWithOtp(verificationId, otp)
            .compose(stateToLce())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ mutableLoginResultState.value = it }, { throwable: Throwable ->
                Timber.w(throwable, "Error verifying")
                mutableLoginResultState.value = Lce.Error(throwable)
            })
            .run { disposables.add(this) }
    }


    private fun stateToLce() = ObservableTransformer<LoginResultState, Lce<Event<LoginResultState>>> { upstream ->
        upstream.map { result ->
            val lce: Lce<Event<LoginResultState>> = Lce.Content(Event(result))
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