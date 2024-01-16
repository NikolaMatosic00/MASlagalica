package com.example.igricaslagalica.controller.auth

interface AuthListener {
    fun onAuthSuccess()
    fun onAuthFailed(message: String)
}