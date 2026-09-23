package com.example

import com.example.util.PersianFormatters
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun test_toPersianDigits() {
    val result = PersianFormatters.toPersianDigits("1234567890, %")
    assertEquals("۱۲۳۴۵۶۷۸۹۰٬ ٪", result)
  }

  @Test
  fun test_formatPrice() {
    val result = PersianFormatters.formatPrice(231500.0)
    assertEquals("۲۳۱٬۵۰۰", result)
  }

  @Test
  fun test_formatPercentage() {
    val pos = PersianFormatters.formatPercentage(1.04)
    assertEquals("+۱.۰۴٪", pos)

    val neg = PersianFormatters.formatPercentage(-0.3)
    assertEquals("-۰.۳٪", neg)
  }

  @Test
  fun test_marketCategory() {
    val gold = com.example.data.model.MarketCategory.fromCode("GOLD")
    assertEquals(com.example.data.model.MarketCategory.GOLD, gold)
    assertEquals("قیمت طلا", gold.title)

    val currency = com.example.data.model.MarketCategory.fromCode("CURRENCY")
    assertEquals(com.example.data.model.MarketCategory.CURRENCY, currency)
    assertEquals("قیمت دلار", currency.title)

    val crypto = com.example.data.model.MarketCategory.fromCode("CRYPTO")
    assertEquals(com.example.data.model.MarketCategory.CRYPTO, crypto)
    assertEquals("ارز دیجیتال", crypto.title)
  }

  @Test
  fun test_converterHelpers() {
    // Parsing Persian digits
    val parsed1 = PersianFormatters.parseUserInputToDouble("۵۰,۰۰۰,۰۰۰")
    assertEquals(50000000.0, parsed1!!, 0.001)

    val parsed2 = PersianFormatters.parseUserInputToDouble("2.5")
    assertEquals(2.5, parsed2!!, 0.001)

    val parsed3 = PersianFormatters.parseUserInputToDouble("۱.۷۵")
    assertEquals(1.75, parsed3!!, 0.001)

    // Decimal formatting
    val formatted = PersianFormatters.formatDecimal(216.264, maxFractionDigits = 2)
    assertEquals("۲۱۶.۲۶", formatted)

    // Calculation verification: 50,000,000 Toman / 231,200 (Dollar)
    val dollars = 50000000.0 / 231200.0
    val roundedDollars = PersianFormatters.formatDecimal(dollars, 2)
    assertEquals("۲۱۶.۲۶", roundedDollars)
  }

  @Test
  fun test_historyRange() {
    val weekly = com.example.ui.components.HistoryRange.WEEKLY
    assertEquals(7, weekly.daysCount)
    assertEquals("هفتگی (۷ روز)", weekly.title)

    val monthly = com.example.ui.components.HistoryRange.MONTHLY
    assertEquals(30, monthly.daysCount)
    assertEquals("ماهانه (۳۰ روز)", monthly.title)
  }

  @Test
  fun test_widgetActionConstant() {
    assertEquals("com.example.widget.ACTION_REFRESH_WIDGET", com.example.widget.MarketRatesWidgetProvider.ACTION_REFRESH_WIDGET)
  }

  @Test
  fun test_trendStatus24h() {
    val positivePct = 1.45
    val negativePct = -0.85
    val zeroPct = 0.0

    assertTrue(positivePct > 0.0)
    assertTrue(negativePct < 0.0)
    assertEquals("+۱.۴۵٪", PersianFormatters.formatPercentage(positivePct))
    assertEquals("-۰.۸۵٪", PersianFormatters.formatPercentage(negativePct))
  }

  @Test
  fun test_shareContentFormat() {
    val rate = com.example.data.local.MarketRateEntity(
      id = "USD",
      name = "دلار آمریکا",
      symbol = "USD",
      price = 61200.0,
      previousPrice = 60500.0,
      changePercent = 1.15,
      changeAmount = 700.0,
      highPrice = 61500.0,
      lowPrice = 60800.0,
      unit = "تومان",
      category = "CURRENCY",
      provider = "بازار آزاد",
      updatedAt = System.currentTimeMillis()
    )

    val formattedPrice = PersianFormatters.formatPrice(rate.price)
    assertEquals("۶۱٬۲۰۰", formattedPrice)
    val formattedPct = PersianFormatters.formatPercentage(rate.changePercent)
    assertEquals("+۱.۱۵٪", formattedPct)
  }
}

