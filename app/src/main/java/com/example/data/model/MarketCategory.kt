package com.example.data.model

enum class MarketCategory(val title: String, val code: String) {
    GOLD("قیمت طلا", "GOLD"),
    CURRENCY("قیمت دلار", "CURRENCY"),
    CRYPTO("ارز دیجیتال", "CRYPTO");

    companion object {
        fun fromCode(code: String): MarketCategory = entries.firstOrNull { it.code == code } ?: GOLD
    }
}
