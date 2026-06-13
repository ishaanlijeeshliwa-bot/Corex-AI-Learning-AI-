package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.CorexMainLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge transparent system bars
        enableEdgeToEdge()
        
        // Ensure robust ViewModel instantiations
        val viewModel = ViewModelProvider(this)[StudyViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                CorexMainLayout(viewModel = viewModel)
            }
        }
    }
}
