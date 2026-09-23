package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.data.local.MarketRateEntity
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

object MarketShareHelper {

    /**
     * Shares rate information as formatted Persian text via Android Sharesheet
     */
    fun shareRateText(context: Context, rate: MarketRateEntity) {
        val pct = rate.changePercent ?: 0.0
        val trendEmoji = if (pct >= 0) "📈" else "📉"
        val trendWord = if (pct >= 0) "صعودی ↗" else "نزولی ↘"
        val timeStr = PersianFormatters.formatTime(rate.updatedAt)

        val message = buildString {
            appendLine("$trendEmoji قیمت لحظه‌ای بازار")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("نام دارایی: ${rate.name} (${rate.symbol})")
            appendLine("نرخ لحظه‌ای: ${PersianFormatters.formatPrice(rate.price)} ${rate.unit}")
            appendLine("تغییرات ۲۴ ساعته: ${PersianFormatters.formatPercentage(pct)} ($trendWord)")
            if (rate.highPrice != null && rate.lowPrice != null) {
                appendLine("بالاترین قیمت امروز: ${PersianFormatters.formatPrice(rate.highPrice)} ${rate.unit}")
                appendLine("پایین‌ترین قیمت امروز: ${PersianFormatters.formatPrice(rate.lowPrice)} ${rate.unit}")
            }
            appendLine("ساعت استعلام: $timeStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("📱 استعلام لحظه‌ای طلا، سکه، دلار و کریپتو با اپلیکیشن «قیمت بازار»")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_TITLE, "قیمت لحظه‌ای ${rate.name}")
            type = "text/plain"
        }

        val chooser = Intent.createChooser(sendIntent, "اشتراک‌گذاری قیمت ${rate.name}")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Generates a sleek, high-resolution visual card image with chart and shares it to social networks
     */
    fun shareRateCardWithChartImage(context: Context, rate: MarketRateEntity) {
        try {
            val width = 1080
            val height = 720
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Dark gradient card background
            val bgPaint = Paint().apply {
                color = Color.parseColor("#12151C")
                isAntiAlias = true
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Inner card box with rounded corners
            val cardPaint = Paint().apply {
                color = Color.parseColor("#181D26")
                isAntiAlias = true
            }
            val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
            canvas.drawRoundRect(cardRect, 36f, 36f, cardPaint)

            // Card border in subtle gold
            val borderPaint = Paint().apply {
                color = Color.parseColor("#323B4C")
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawRoundRect(cardRect, 36f, 36f, borderPaint)

            // Text Paint for App Header
            val headerPaint = Paint().apply {
                color = Color.parseColor("#E5A93C")
                textSize = 34f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText("قیمت بازار • نرخ زنده و لحظه‌ای", 80f, 110f, headerPaint)

            // Rate Name
            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 52f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText(rate.name, 80f, 190f, namePaint)

            // Symbol / Category
            val subPaint = Paint().apply {
                color = Color.parseColor("#8C98AC")
                textSize = 28f
                isAntiAlias = true
            }
            canvas.drawText(rate.symbol, 80f, 235f, subPaint)

            // Big Live Price
            val pricePaint = Paint().apply {
                color = Color.WHITE
                textSize = 68f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            val priceText = PersianFormatters.formatPrice(rate.price) + " " + rate.unit
            canvas.drawText(priceText, 80f, 325f, pricePaint)

            // 24H Trend Status Badge
            val pct = rate.changePercent ?: 0.0
            val isPositive = pct >= 0
            val trendColor = if (isPositive) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            val trendBgColor = if (isPositive) Color.parseColor("#0F331A") else Color.parseColor("#3B1010")

            val badgeRect = RectF(80f, 355f, 420f, 420f)
            val badgePaint = Paint().apply {
                color = trendBgColor
                isAntiAlias = true
            }
            canvas.drawRoundRect(badgeRect, 18f, 18f, badgePaint)

            val badgeBorderPaint = Paint().apply {
                color = trendColor
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                isAntiAlias = true
            }
            canvas.drawRoundRect(badgeRect, 18f, 18f, badgeBorderPaint)

            val badgeTextPaint = Paint().apply {
                color = trendColor
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            val trendLabel = if (isPositive) "صعودی" else "نزولی"
            canvas.drawText(
                "۲۴س: $trendLabel ${PersianFormatters.formatPercentage(pct)}",
                105f,
                400f,
                badgeTextPaint
            )

            // Draw Chart Sparkline Curve
            val chartPaint = Paint().apply {
                color = trendColor
                style = Paint.Style.STROKE
                strokeWidth = 6f
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

            val path = Path()
            val chartStartX = 80f
            val chartEndX = width - 80f
            val chartBaseY = 560f
            val chartHeight = 110f

            // Generate representative curve based on trend
            val pointsCount = 10
            for (i in 0 until pointsCount) {
                val x = chartStartX + (i.toFloat() / (pointsCount - 1)) * (chartEndX - chartStartX)
                val normalizedY = if (isPositive) {
                    0.8f - (i.toFloat() / pointsCount) * 0.6f + (if (i % 2 == 0) 0.1f else -0.1f)
                } else {
                    0.2f + (i.toFloat() / pointsCount) * 0.6f + (if (i % 2 == 0) -0.1f else 0.1f)
                }
                val y = chartBaseY - normalizedY * chartHeight
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, chartPaint)

            // Footer Timestamp & Brand
            val footerPaint = Paint().apply {
                color = Color.parseColor("#637188")
                textSize = 26f
                isAntiAlias = true
            }
            val timeText = "بروزرسانی: ساعت ${PersianFormatters.formatTime(rate.updatedAt)} • بازار آزاد"
            canvas.drawText(timeText, 80f, height - 70f, footerPaint)

            // Save Bitmap to Cache Directory
            val imagesDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val imageFile = File(imagesDir, "rate_${rate.id}_share.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Share Image via FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📊 استعلام نرخ لحظه‌ای و نمودار ${rate.name}: ${PersianFormatters.formatPrice(rate.price)} ${rate.unit} (${PersianFormatters.formatPercentage(pct)})"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری تصویر نرخ ${rate.name}")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Graceful fallback to rich text share if image generation/provider throws
            shareRateText(context, rate)
        }
    }
}
