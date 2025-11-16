package com.example.myapplication.navigation

enum class Screen(val route: String) {
    HOME("home"),
    MENU("menu")
}

enum class Status(val status: String) {
    RECORDING("recording"),
    PLAYING("playing"),
    IDLE("idle")
}