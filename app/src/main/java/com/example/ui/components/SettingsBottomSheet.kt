package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.local.MarketRateEntity
import com.example.ui.theme.LocalAppPalette
import com.example.update.GithubAppUpdater
import com.example.update.GithubReleaseInfo
import com.example.update.UpdateCheckResult
import com.example.util.MarketAssetHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    rates: List<MarketRateEntity>,
    isDarkTheme: Boolean,
    compactList: Boolean,
    alertsEnabled: Boolean,
    widgetSlot1: String,
    widgetSlot2: String,
    widgetSlot3: String,
    widgetSlot4: String,
    onDarkThemeChange: (Boolean) -> Unit,
    onCompactListChange: (Boolean) -> Unit,
    onAlertsEnabledChange: (Boolean) -> Unit,
    onWidgetSlotsChange: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var slot1 by remember(widgetSlot1) { mutableStateOf(widgetSlot1) }
    var slot2 by remember(widgetSlot2) { mutableStateOf(widgetSlot2) }
    var slot3 by remember(widgetSlot3) { mutableStateOf(widgetSlot3) }
    var slot4 by remember(widgetSlot4) { mutableStateOf(widgetSlot4) }
    val options = rates.sortedBy { it.orderIndex }

    var updateChecking by remember { mutableStateOf(false) }
    var updateDownloading by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableIntStateOf(0) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var availableRelease by remember { mutableStateOf<GithubReleaseInfo?>(null) }

    fun checkUpdate(auto: Boolean = false) {
        scope.launch {
            updateChecking = true
            updateStatus = if (auto) null else "در حال بررسی نسخه رسمی…"
            availableRelease = null
            when (val result = GithubAppUpdater.checkForUpdate()) {
                is UpdateCheckResult.Available -> {
                    availableRelease = result.release
                    updateStatus = "نسخه ${result.release.versionName} آماده است"
                }
                is UpdateCheckResult.UpToDate -> {
                    updateStatus = "آخرین نسخه نصب است (${result.currentVersion})"
                }
                is UpdateCheckResult.Failed -> {
                    updateStatus = result.message
                }
            }
            updateChecking = false
        }
    }

    LaunchedEffect(Unit) {
        checkUpdate(auto = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.background,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                palette.accent.copy(alpha = 0.18f),
                                palette.card
                            )
                        )
                    )
                    .border(1.dp, palette.border.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "تنظیمات",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نسخه فعلی ${BuildConfig.VERSION_NAME}",
                        color = palette.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SettingsSectionCard(
                icon = Icons.Default.SystemUpdate,
                title = "به‌روزرسانی برنامه",
                subtitle = "نسخه رسمی GitHub با نصب مستقیم"
            ) {
                Text(
                    text = updateStatus ?: "آماده بررسی",
                    color = if (availableRelease != null) palette.accent else palette.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (availableRelease != null) FontWeight.SemiBold else FontWeight.Normal
                )

                availableRelease?.let { release ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نسخه ${release.versionName}",
                            color = palette.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = GithubAppUpdater.formatSize(release.apkSizeBytes),
                            color = palette.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    if (release.releaseNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = release.releaseNotes,
                            color = palette.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (updateChecking || updateDownloading) {
                    LinearProgressIndicator(
                        progress = { if (updateDownloading) updateProgress / 100f else 0.25f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = palette.accent,
                        trackColor = palette.border
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (updateDownloading) {
                        Text(
                            text = "دانلود $updateProgress٪",
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { checkUpdate(auto = false) },
                        enabled = !updateChecking && !updateDownloading,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("check_update_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (updateChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = palette.accent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("بررسی", color = palette.textPrimary)
                    }

                    Button(
                        onClick = {
                            val release = availableRelease ?: return@Button
                            updateDownloading = true
                            updateProgress = 0
                            updateStatus = "در حال دانلود نسخه ${release.versionName}…"
                            GithubAppUpdater.downloadAndInstall(
                                context = context,
                                release = release,
                                onStarted = {
                                    updateStatus = "دانلود نسخه رسمی شروع شد"
                                },
                                onProgress = { progress ->
                                    updateProgress = progress
                                },
                                onReadyToInstall = {
                                    updateDownloading = false
                                    updateProgress = 100
                                    updateStatus = "دانلود کامل شد؛ نصب را تأیید کنید"
                                },
                                onError = { err ->
                                    updateDownloading = false
                                    updateProgress = 0
                                    if (err == "ALLOW_UNKNOWN_SOURCES") {
                                        updateStatus = "اجازه نصب از این برنامه لازم است"
                                        Toast.makeText(
                                            context,
                                            "اجازه نصب برنامه را فعال کنید",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        GithubAppUpdater.openUnknownSourcesSettings(context)
                                    } else {
                                        updateStatus = err
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        },
                        enabled = availableRelease != null && !updateChecking && !updateDownloading,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_update_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = palette.background,
                            disabledContainerColor = palette.border,
                            disabledContentColor = palette.textSecondary
                        )
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (updateDownloading) "$updateProgress٪" else "دانلود و نصب")
                    }
                }

                TextButton(
                    onClick = { GithubAppUpdater.openReleasePage(context) },
                    modifier = Modifier.testTag("open_github_release_button")
                ) {
                    Text("مشاهده Releaseها در GitHub", color = palette.accent, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSectionCard(
                icon = Icons.Default.DarkMode,
                title = "ظاهر",
                subtitle = "تم و تراکم نمایش"
            ) {
                SettingsToggleRow(
                    title = "حالت تاریک",
                    checked = isDarkTheme,
                    onCheckedChange = onDarkThemeChange
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggleRow(
                    title = "لیست فشرده",
                    checked = compactList,
                    onCheckedChange = onCompactListChange
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSectionCard(
                icon = Icons.Default.Notifications,
                title = "اعلان‌ها",
                subtitle = "هشدار نوسان قیمت"
            ) {
                SettingsToggleRow(
                    title = "اعلان تغییر قیمت",
                    checked = alertsEnabled,
                    onCheckedChange = onAlertsEnabledChange
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSectionCard(
                icon = Icons.Default.Widgets,
                title = "ویجت",
                subtitle = "چهار دارایی صفحه اصلی"
            ) {
                WidgetSlotPicker(
                    label = "اصلی ۱",
                    selectedId = slot1,
                    options = options,
                    onSelect = {
                        slot1 = it
                        onWidgetSlotsChange(slot1, slot2, slot3, slot4)
                    }
                )
                WidgetSlotPicker(
                    label = "اصلی ۲",
                    selectedId = slot2,
                    options = options,
                    onSelect = {
                        slot2 = it
                        onWidgetSlotsChange(slot1, slot2, slot3, slot4)
                    }
                )
                WidgetSlotPicker(
                    label = "فرعی ۱",
                    selectedId = slot3,
                    options = options,
                    onSelect = {
                        slot3 = it
                        onWidgetSlotsChange(slot1, slot2, slot3, slot4)
                    }
                )
                WidgetSlotPicker(
                    label = "فرعی ۲",
                    selectedId = slot4,
                    options = options,
                    onSelect = {
                        slot4 = it
                        onWidgetSlotsChange(slot1, slot2, slot3, slot4)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.card)
            .border(1.dp, palette.border.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(palette.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    color = palette.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.background,
                checkedTrackColor = palette.accent,
                uncheckedThumbColor = palette.textSecondary,
                uncheckedTrackColor = palette.border
            )
        )
    }
}

@Composable
private fun WidgetSlotPicker(
    label: String,
    selectedId: String,
    options: List<MarketRateEntity>,
    onSelect: (String) -> Unit
) {
    val palette = LocalAppPalette.current
    val selected = options.find { it.id == selectedId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.cardSecondary)
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected?.let {
                    "${MarketAssetHelper.getAssetIcon(it.id)} ${it.name}"
                } ?: selectedId,
                color = palette.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            TextButton(
                onClick = {
                    if (options.isEmpty()) return@TextButton
                    val idx = options.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
                    onSelect(options[(idx + 1) % options.size].id)
                }
            ) {
                Text("تغییر", color = palette.accent, fontSize = 12.sp)
            }
        }
    }
}
