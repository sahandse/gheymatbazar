package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MarketRateEntity
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RateIncrease
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianFormatters

enum class ConversionDirection(val title: String) {
    TOMAN_TO_ASSET("تبدیل تومان به دارایی"),
    ASSET_TO_TOMAN("تبدیل دارایی به تومان")
}

data class ConverterAssetOption(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val unit: String
)

private val CONVERTER_ASSETS = listOf(
    ConverterAssetOption("USD", "دلار آمریکا", "🇺🇸", "دلار"),
    ConverterAssetOption("GOLD_18K", "طلای ۱۸ عیار", "🥇", "گرم"),
    ConverterAssetOption("COIN_EMAMI", "سکه امامی", "🪙", "عدد سکه"),
    ConverterAssetOption("GOLD_MESGHAL", "مثقال طلا", "⚖️", "مثقال"),
    ConverterAssetOption("USDT", "تتر", "₮", "تتر"),
    ConverterAssetOption("EUR", "یورو", "🇪🇺", "یورو")
)

@Composable
fun MarketConverterCard(
    rates: List<MarketRateEntity>,
    modifier: Modifier = Modifier
) {
    if (rates.isEmpty()) return

    var isExpanded by remember { mutableStateOf(true) }
    var direction by remember { mutableStateOf(ConversionDirection.TOMAN_TO_ASSET) }
    var selectedAssetId by remember { mutableStateOf("USD") }
    var rawInput by remember { mutableStateOf("50000000") } // Default 50 million tomans

    val focusManager = LocalFocusManager.current

    // Find the rate for the selected asset
    val targetRateEntity = rates.find { it.id == selectedAssetId }
        ?: rates.firstOrNull { it.id == "USD" }
        ?: rates.first()

    val currentAssetOption = CONVERTER_ASSETS.find { it.id == selectedAssetId }
        ?: CONVERTER_ASSETS.first()

    // Parse input
    val numericInput = PersianFormatters.parseUserInputToDouble(rawInput) ?: 0.0

    // Compute conversion
    val unitPrice = targetRateEntity.price
    val calculationResult = remember(numericInput, unitPrice, direction) {
        if (unitPrice <= 0 || numericInput <= 0) {
            0.0
        } else {
            when (direction) {
                ConversionDirection.TOMAN_TO_ASSET -> numericInput / unitPrice
                ConversionDirection.ASSET_TO_TOMAN -> numericInput * unitPrice
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF161B24),
                        Color(0xFF0F1218)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2E384A),
                        Color(0xFF1A1F2B)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("currency_converter_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Icon + Title + Expand/Collapse toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = !isExpanded }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "محاسبه‌گر و تبدیل ارزش",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "تبدیل تومان به دلار، طلا، سکه و تتر با نرخ زنده",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "بستن" else "باز کردن",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Direction Switcher Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF11141A))
                            .border(1.dp, Color(0xFF222834), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DirectionTab(
                            title = "تومان به دارایی",
                            isSelected = direction == ConversionDirection.TOMAN_TO_ASSET,
                            onClick = { direction = ConversionDirection.TOMAN_TO_ASSET },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        DirectionTab(
                            title = "دارایی به تومان",
                            isSelected = direction == ConversionDirection.ASSET_TO_TOMAN,
                            onClick = { direction = ConversionDirection.ASSET_TO_TOMAN },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Asset Selector Chips
                    Text(
                        text = "انتخاب دارایی مقصد / مبدا:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CONVERTER_ASSETS) { asset ->
                            val isSelected = asset.id == selectedAssetId
                            val chipBg = if (isSelected) GoldAccent else Color(0xFF141820)
                            val chipBorder = if (isSelected) GoldAccent else Color(0xFF252C3A)
                            val textColor = if (isSelected) DarkBackground else TextPrimary

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedAssetId = asset.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = asset.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = asset.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Field
                    val inputLabel = when (direction) {
                        ConversionDirection.TOMAN_TO_ASSET -> "مبلغ به تومان:"
                        ConversionDirection.ASSET_TO_TOMAN -> "مقدار ${currentAssetOption.title} (${currentAssetOption.unit}):"
                    }

                    Text(
                        text = inputLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_input_field"),
                        placeholder = {
                            Text(
                                text = if (direction == ConversionDirection.TOMAN_TO_ASSET) "مثلاً ۵۰,۰۰۰,۰۰۰" else "مثلاً ۱۰۰",
                                color = TextSecondary.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            if (rawInput.isNotEmpty()) {
                                IconButton(onClick = { rawInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "پاک کردن",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF101319),
                            unfocusedContainerColor = Color(0xFF101319),
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF262C3A),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (direction == ConversionDirection.TOMAN_TO_ASSET) {
                            listOf(
                                "۱۰ م" to "10000000",
                                "۵۰ م" to "50000000",
                                "۱۰۰ م" to "100000000",
                                "۵۰۰ م" to "500000000"
                            ).forEach { (label, value) ->
                                PresetChip(
                                    label = label,
                                    onClick = { rawInput = value },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            when (selectedAssetId) {
                                "GOLD_18K" -> listOf(
                                    "۱ گرم" to "1",
                                    "۵ گرم" to "5",
                                    "۱۰ گرم" to "10",
                                    "۱۰۰ گرم" to "100"
                                )
                                "COIN_EMAMI" -> listOf(
                                    "۱ سکه" to "1",
                                    "۲ سکه" to "2",
                                    "۵ سکه" to "5",
                                    "۱۰ سکه" to "10"
                                )
                                "GOLD_MESGHAL" -> listOf(
                                    "۱ مثقال" to "1",
                                    "۲ مثقال" to "2",
                                    "۵ مثقال" to "5",
                                    "۱۰ مثقال" to "10"
                                )
                                else -> listOf(
                                    "۱۰۰" to "100",
                                    "۵۰۰" to "500",
                                    "۱,۰۰۰" to "1000",
                                    "۵,۰۰۰" to "5000"
                                )
                            }.forEach { (label, value) ->
                                PresetChip(
                                    label = label,
                                    onClick = { rawInput = value },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result Box (Card with Gold Accent Border)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1D222E),
                                        Color(0xFF141720)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        GoldAccent.copy(alpha = 0.5f),
                                        Color(0xFF262C3A)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                            .testTag("converter_result_box")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "نتیجه تبدیل:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 11.5.sp
                                )

                                Text(
                                    text = "نرخ پایه: ${PersianFormatters.formatPrice(unitPrice)} تومان",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val resultText = when (direction) {
                                    ConversionDirection.TOMAN_TO_ASSET -> {
                                        if (selectedAssetId == "USD" || selectedAssetId == "EUR" || selectedAssetId == "USDT") {
                                            PersianFormatters.formatDecimal(calculationResult, maxFractionDigits = 2)
                                        } else {
                                            PersianFormatters.formatDecimal(calculationResult, maxFractionDigits = 3)
                                        }
                                    }
                                    ConversionDirection.ASSET_TO_TOMAN -> {
                                        PersianFormatters.formatPrice(calculationResult)
                                    }
                                }

                                Text(
                                    text = resultText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                val unitLabel = when (direction) {
                                    ConversionDirection.TOMAN_TO_ASSET -> currentAssetOption.unit
                                    ConversionDirection.ASSET_TO_TOMAN -> "تومان"
                                }

                                Text(
                                    text = unitLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Formula breakdown explanation
                            val formulaNote = when (direction) {
                                ConversionDirection.TOMAN_TO_ASSET -> {
                                    "معادل خرید ${PersianFormatters.formatDecimal(calculationResult, 2)} ${currentAssetOption.unit} با سرمایه شما"
                                }
                                ConversionDirection.ASSET_TO_TOMAN -> {
                                    "ارزش ریالی معادل برای فروش یا دارایی شما"
                                }
                            }

                            Text(
                                text = formulaNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "dirBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) DarkBackground else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "dirText"
    )

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAccent.copy(alpha = 0.2f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF171B24))
            .border(1.dp, Color(0xFF262C3A), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GoldAccent.copy(alpha = 0.15f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.5.sp
        )
    }
}
