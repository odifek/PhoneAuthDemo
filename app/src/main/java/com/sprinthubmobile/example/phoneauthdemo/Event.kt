package com.sprinthubmobile.example.phoneauthdemo

data class Event<T>(private val content: T) {
    private var _isHandled: Boolean = false

    val isHandled: Boolean
        get() = _isHandled

    fun getContentIfNotHandled(): T? {
        return if (!_isHandled) {
            _isHandled = true
            content
        } else null
    }

    fun peekContent(): T = content
}