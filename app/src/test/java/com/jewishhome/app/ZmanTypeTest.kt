package com.jewishhome.app

import com.jewishhome.app.domain.model.ZmanType
import org.junit.Assert.*
import org.junit.Test

class ZmanTypeTest {

    @Test
    fun `all zman types have Hebrew names`() {
        ZmanType.entries.forEach { zmanType ->
            assertTrue(
                "ZmanType ${zmanType.name} should have Hebrew name",
                zmanType.hebrewName.isNotBlank()
            )
        }
    }

    @Test
    fun `all zman types have English names`() {
        ZmanType.entries.forEach { zmanType ->
            assertTrue(
                "ZmanType ${zmanType.name} should have English name",
                zmanType.englishName.isNotBlank()
            )
        }
    }

    @Test
    fun `zman types are in correct order`() {
        val types = ZmanType.entries.sortedBy { it.order }

        // First should be dawn
        assertEquals(ZmanType.ALOT_HASHACHAR, types.first())

        // Last should be midnight
        assertEquals(ZmanType.CHATZOT_LAYLA, types.last())

        // Sunrise should be before sunset
        val sunriseIndex = types.indexOf(ZmanType.NETZ_HACHAMA)
        val sunsetIndex = types.indexOf(ZmanType.SHKIA)
        assertTrue(sunriseIndex < sunsetIndex)
    }

    @Test
    fun `fromOrder returns correct zman type`() {
        assertEquals(ZmanType.ALOT_HASHACHAR, ZmanType.fromOrder(1))
        assertEquals(ZmanType.NETZ_HACHAMA, ZmanType.fromOrder(3))
        assertEquals(ZmanType.SHKIA, ZmanType.fromOrder(12))
        assertNull(ZmanType.fromOrder(100))
    }

    @Test
    fun `each order value is unique`() {
        val orders = ZmanType.entries.map { it.order }
        assertEquals(orders.size, orders.distinct().size)
    }
}
