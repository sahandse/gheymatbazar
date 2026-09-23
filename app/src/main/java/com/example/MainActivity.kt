package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MarketRatesViewModel
import com.example.ui.components.SplashScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.LocalAppPalette
import com.example.ui.theme.MyApplicationTheme
import com.example.update.GithubAppUpdater
import com.example.update.GithubReleaseInfo
import com.example.update.UpdateCheckResult
import com.example.util.PriceAlertNotifier

class MainActivity : ComponentActivity() {

    private val viewModel: MarketRatesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PriceAlertNotifier.ensureChannel(this)
        requestNotificationPermissionIfNeeded()

        intent.getStringExtra(PriceAlertNotifier.EXTRA_OPEN_RATE_ID)?.let { rateId ->
            viewModel.selectRate(rateId)
        }

        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MarketRatesApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS
            )
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 1001
    }
}

@Composable
fun MarketRatesApp(viewModel: MarketRatesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    var isSplashVisible by remember { mutableStateOf(true) }
    var availableUpdate by remember { mutableStateOf<GithubReleaseInfo?>(null) }
    var updatePromptDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(isSplashVisible) {
        if (!isSplashVisible && !updatePromptDismissed) {
            when (val result = GithubAppUpdater.checkForUpdate()) {
                is UpdateCheckResult.Available -> availableUpdate = result.release
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        if (isSplashVisible) {
            SplashScreen(onTimeout = { isSplashVisible = false })
        } else {
            BackHandler(enabled = uiState.selectedRateId != null) {
                viewModel.selectRate(null)
            }
            BackHandler(enabled = uiState.selectedRateId == null && uiState.showSettings) {
                viewModel.setShowSettings(false)
            }
            BackHandler(enabled = uiState.selectedRateId == null && uiState.showConverter) {
                viewModel.setShowConverter(false)
            }

            AnimatedContent(
                targetState = uiState.selectedRateId,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screenTransition"
            ) { selectedId ->
                if (selectedId != null) {
                    val rate = uiState.rates.find { it.id == selectedId }
                    if (rate != null) {
                        DetailScreen(
                            rate = rate,
                            isFavorite = rate.id in uiState.favoriteIds,
                            isAlertEnabled = rate.id in uiState.alertRateIds && uiState.alertsEnabled,
                            onBackClick = { viewModel.selectRate(null) },
                            onToggleFavorite = { viewModel.toggleFavorite(rate.id) },
                            onToggleAlert = {
                                val currentlyOn =
                                    rate.id in uiState.alertRateIds && uiState.alertsEnabled
                                viewModel.setAlertForRate(rate.id, !currentlyOn)
                            }
                        )
                    } else {
                        HomeContent(viewModel = viewModel)
                    }
                } else {
                    HomeContent(viewModel = viewModel)
                }
            }
        }
    }

    val release = availableUpdate
    if (!isSplashVisible && release != null && !updatePromptDismissed) {
        AlertDialog(
            onDismissRequest = {
                updatePromptDismissed = true
                availableUpdate = null
            },
            title = { Text("نسخه ${release.versionName} آماده است") },
            text = {
                Text(
                    buildString {
                        append("حجم: ${GithubAppUpdater.formatSize(release.apkSizeBytes)}")
                        if (release.releaseNotes.isNotBlank()) {
                            append("\n\n")
                            append(release.releaseNotes.take(500))
                        }
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        GithubAppUpdater.downloadAndInstall(
                            context = context,
                            release = release,
                            onStarted = {
                                Toast.makeText(context, "دانلود آپدیت شروع شد", Toast.LENGTH_SHORT).show()
                                updatePromptDismissed = true
                                availableUpdate = null
                            },
                            onError = { error ->
                                if (error == "ALLOW_UNKNOWN_SOURCES") {
                                    GithubAppUpdater.openUnknownSourcesSettings(context)
                                    Toast.makeText(context, "اجازه نصب برنامه را فعال کنید", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                ) {
                    Text("دانلود و نصب")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        updatePromptDismissed = true
                        availableUpdate = null
                    }
                ) {
                    Text("بعداً")
                }
            }
        )
    }
}

@Composable
private fun HomeContent(viewModel: MarketRatesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onCategorySelected = { viewModel.selectCategory(it) },
        onRateClick = { viewModel.selectRate(it) },
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onToggleConverter = { viewModel.setShowConverter(!uiState.showConverter) },
        onOpenSettings = { viewModel.setShowSettings(true) },
        onCloseSettings = { viewModel.setShowSettings(false) },
        onCloseConverter = { viewModel.setShowConverter(false) },
        onDarkThemeChange = { viewModel.setDarkTheme(it) },
        onCompactListChange = { viewModel.setCompactList(it) },
        onAlertsEnabledChange = { viewModel.setAlertsEnabled(it) },
        onWidgetSlotsChange = { a, b, c, d -> viewModel.setWidgetSlots(a, b, c, d) }
    )
}
