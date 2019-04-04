package com.sprinthubmobile.example.phoneauthdemo.login

sealed class Lce<T> {

    data class Loading<T>(val loading: Boolean): Lce<T>()

    data class Content<T>(val data: T): Lce<T>()

    data class Error<T>(val throwable: Throwable): Lce<T>()

}
