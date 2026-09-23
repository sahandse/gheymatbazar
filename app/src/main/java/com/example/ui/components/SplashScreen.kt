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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val logoScale = remember { Animatable(0.72f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(18f) }
    val ringAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            ringAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        }
        delay(280)
        launch {
            titleAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            titleOffset.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        delay(900)
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
                    colors = listOf(palette.accent.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.42f),
                    radius = size.minDimension * 0.55f
                ),
                center = Offset(size.width / 2f, size.height * 0.42f),
                radius = size.minDimension * 0.55f
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(logoScale.value)
                        .alpha(ringAlpha.value * 0.55f)
                        .background(
                            Brush.radialGradient(
                                listOf(palette.accent.copy(alpha = 0.28f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_market_rates),
                    contentDescription = "قیمت بازار",
                    modifier = Modifier
                        .size(92.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "قیمت بازار",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "نرخ زنده طلا، ارز و کریپتو",
                color = palette.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.alpha(titleAlpha.value * 0.9f)
            )
        }
    }
}
