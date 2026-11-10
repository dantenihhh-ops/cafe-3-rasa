package com.cafe3rasa.kasir

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Dashboard Kasir — Cafe 3 Rasa",
        state = rememberWindowState(width = 1280.dp, height = 850.dp)
    ) {
        App()
    }
}
