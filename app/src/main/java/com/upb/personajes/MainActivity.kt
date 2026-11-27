package com.upb.personajes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.upb.personajes.navigation.AppNavigation
import com.upb.personajes.ui.theme.PersonajesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonajesTheme {
                AppNavigation()
            }
        }
    }
}
