package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.theme.LocalAppPalette
import com.example.util.MarketAssetHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    rates: List<MarketRateEntity>,
    isDarkTheme: Boolean,
    alertsEnabled: Boolean,
    widgetSlot1: String,
    widgetSlot2: String,
    widgetSlot3: String,
    widgetSlot4: String,
    onDarkThemeChange: (Boolean) -> Unit,
    onAlertsEnabledChange: (Boolean) -> Unit,
    onWidgetSlotsChange: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalAppPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var slot1 by remember(widgetSlot1) { mutableStateOf(widgetSlot1) }
    var slot2 by remember(widgetSlot2) { mutableStateOf(widgetSlot2) }
    var slot3 by remember(widgetSlot3) { mutableStateOf(widgetSlot3) }
    var slot4 by remember(widgetSlot4) { mutableStateOf(widgetSlot4) }
    val options = rates.sortedBy { it.orderIndex }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.card,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "تنظیمات",
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            SettingsToggleRow(
                title = "حالت تاریک",
                checked = isDarkTheme,
                onCheckedChange = onDarkThemeChange
            )
            SettingsToggleRow(
                title = "اعلان تغییر قیمت",
                checked = alertsEnabled,
                onCheckedChange = onAlertsEnabledChange
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = palette.border)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "دارایی‌های ویجت",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "چهار نرخ نمایش‌داده‌شده روی ویجت صفحه اصلی",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

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
            Spacer(modifier = Modifier.height(24.dp))
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.cardSecondary, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected?.let {
                    "${MarketAssetHelper.getAssetIcon(it.id)} ${it.name}"
                } ?: selectedId,
                color = palette.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            TextButton(
                onClick = {
                    if (options.isEmpty()) return@TextButton
                    val idx = options.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
                    onSelect(options[(idx + 1) % options.size].id)
                }
            ) {
                Text("تغییر", color = palette.accent)
            }
        }
    }
}
