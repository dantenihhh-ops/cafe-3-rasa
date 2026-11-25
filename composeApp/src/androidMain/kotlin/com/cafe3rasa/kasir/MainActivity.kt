package com.cafe3rasa.kasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.cafe3rasa.kasir.data.AndroidStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Siapkan SharedPreferences untuk sesi login & alamat server
        AndroidStorage.init(applicationContext)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun KasirDashboardTabletPreview() {
    App()
}

@Preview
@Composable
fun KasirDashboardPhonePreview() {
    App()
}
