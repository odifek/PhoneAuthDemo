package com.sprinthubmobile.example.phoneauthdemo.login

sealed class LoginResultState {
    class CodeSent(): LoginResultState()
}
