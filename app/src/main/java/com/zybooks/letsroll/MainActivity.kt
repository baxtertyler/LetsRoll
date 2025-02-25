package com.zybooks.letsroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zybooks.letsroll.ui.HomeScreen
import com.zybooks.letsroll.ui.GameScreen
import com.zybooks.letsroll.ui.LetsRollApp
import com.zybooks.letsroll.ui.theme.LetsRollTheme
import com.zybooks.letsroll.ui.theme.backgroundColor



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetsRollTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                   LetsRollApp()
                }
            }
        }
    }
}