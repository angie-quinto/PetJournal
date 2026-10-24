package com.example.petjournal.domain

import com.example.petjournal.domain.model.WeightConversion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class WeightConversionTest {

    @Test
    fun testGramsToKg() {
        assertEquals(BigDecimal("5.00"), WeightConversion.fromGrams(5000, "KG"))
    }

    @Test
    fun testGramsToLbs() {
        assertEquals(BigDecimal("10.0"), WeightConversion.fromGrams(4536, "LB", 1))
    }

    @Test
    fun convertsEnteredKilogramsToNormalizedGrams() {
        assertEquals(5000, WeightConversion.toGrams("5", "KG"))
    }

    @Test
    fun convertsEnteredPoundsToNormalizedGrams() {
        assertEquals(4536, WeightConversion.toGrams("10", "LB"))
    }

    @Test
    fun testWeightValidation() {
        assertTrue(WeightConversion.toGrams("1.2", "KG")!! > 0)
        assertEquals(null, WeightConversion.toGrams("-1", "KG"))
        assertEquals(null, WeightConversion.toGrams("0", "LB"))
        assertEquals(null, WeightConversion.toGrams("abc", "KG"))
    }
}
