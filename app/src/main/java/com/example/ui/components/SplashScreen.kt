package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAppPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val logoScale = remember { Animatable(0.82f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(14f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }
        launch { logoScale.animateTo(1f, tween(720, easing = FastOutSlowInEasing)) }
        launch { glowAlpha.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        delay(260)
        launch { titleAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
        launch { titleOffset.animateTo(0f, tween(420, easing = FastOutSlowInEasing)) }
        delay(880)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.accent.copy(alpha = 0.16f * glowAlpha.value), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.38f),
                    radius = size.minDimension * 0.72f
                ),
                center = Offset(size.width / 2f, size.height * 0.38f),
                radius = size.minDimension * 0.72f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.increase.copy(alpha = 0.05f * glowAlpha.value), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.7f),
                    radius = size.minDimension * 0.5f
                ),
                center = Offset(size.width * 0.2f, size.height * 0.7f),
                radius = size.minDimension * 0.5f
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .scale(logoScale.value)
                        .alpha(glowAlpha.value * 0.5f)
                        .background(
                            Brush.radialGradient(
                                listOf(palette.accent.copy(alpha = 0.22f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_market_rates),
                    contentDescription = "قیمت بازار",
                    modifier = Modifier
                        .size(88.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "قیمت بازار",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = (-0.4).sp,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "نرخ زنده",
                color = palette.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.alpha(titleAlpha.value * 0.85f)
            )
        }
    }
}
