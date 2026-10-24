package com.example.petjournal.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

object WeightConversion {
    private val gramsPerKilogram = BigDecimal("1000")
    private val gramsPerPound = BigDecimal("453.59237")

    fun toGrams(amount: String, unit: String): Int? = runCatching {
        val value = BigDecimal(amount.trim())
        if (value <= BigDecimal.ZERO) return null
        value.multiply(if (isPounds(unit)) gramsPerPound else gramsPerKilogram)
            .setScale(0, RoundingMode.HALF_UP)
            .intValueExact()
            .takeIf { it > 0 }
    }.getOrNull()

    fun fromGrams(grams: Int, unit: String, scale: Int = 2): BigDecimal {
        val divisor = if (isPounds(unit)) gramsPerPound else gramsPerKilogram
        return BigDecimal(grams).divide(divisor, scale, RoundingMode.HALF_UP)
    }

    private fun isPounds(unit: String): Boolean =
        unit.equals("LB", ignoreCase = true) || unit.equals("LBS", ignoreCase = true)
}
