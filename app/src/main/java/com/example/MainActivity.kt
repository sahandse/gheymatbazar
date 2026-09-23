package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MarketRatesViewModel
import com.example.ui.components.SplashScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MarketRatesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MarketRatesApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MarketRatesApp(viewModel: MarketRatesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSplashVisible by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (isSplashVisible) {
            SplashScreen(
                onTimeout = { isSplashVisible = false }
            )
        } else {
            // Handle system back gesture
            BackHandler(enabled = uiState.selectedRateId != null) {
                viewModel.selectRate(null)
            }

            AnimatedContent(
                targetState = uiState.selectedRateId,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screenTransition"
            ) { selectedId ->
                if (selectedId != null) {
                    val rate = uiState.rates.find { it.id == selectedId }
                    if (rate != null) {
                        DetailScreen(
                            rate = rate,
                            onBackClick = { viewModel.selectRate(null) }
                        )
                    } else {
                        HomeScreen(
                            uiState = uiState,
                            onRefresh = { viewModel.refresh() },
                            onCategorySelected = { viewModel.selectCategory(it) },
                            onRateClick = { viewModel.selectRate(it) }
                        )
                    }
                } else {
                    HomeScreen(
                        uiState = uiState,
                        onRefresh = { viewModel.refresh() },
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onRateClick = { viewModel.selectRate(it) }
                    )
                }
            }
        }
    }
}
