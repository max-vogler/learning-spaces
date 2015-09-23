package de.maxvogler.learningspaces.services

import de.maxvogler.learningspaces.BaseTest
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.helpers.toWeekday
import de.maxvogler.learningspaces.models.Weekday
import de.maxvogler.learningspaces.models.Weekday.FRIDAY
import de.maxvogler.learningspaces.models.Weekday.MONDAY
import de.maxvogler.learningspaces.models.Weekday.SATURDAY
import de.maxvogler.learningspaces.models.Weekday.SUNDAY
import org.joda.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public open class LocationServiceBaseTest : BaseTest() {

    @Throws(Exception::class)
    public fun testSize() {
        assertEquals(TOTAL_SEATS.size(), locations.size())
    }

    @Throws(Exception::class)
    public fun testNames() {
        TOTAL_SEATS.keySet().forEach { name ->
            assertEquals(1, locations.filterValues { it.name == name }.size());
        }
    }

    @Throws(Exception::class)
    public fun testTotalSeats() {
        TOTAL_SEATS.forEach { data ->
            val name = data.getKey()
            val expectedSeats: Int = data.getValue()
            val seats: Int = locations.values().first { it.name == name }.totalSeats

            assertEquals(expectedSeats, seats, "$name has $seats instead of expected $expectedSeats seats")
        }
    }

    @Throws(Exception::class)
    public fun testOpeningHours() {
        val ib = locations["FBI"]!!
        val oh = ib.openingHours

        val baseDate = LocalDateTime(2014, 12, 14, 0, 17)
        assertEquals(SUNDAY, baseDate.dayOfWeek.toWeekday())

        (MONDAY..FRIDAY).forEach {
            val date = baseDate.plusDays(it.weekday)
            assertEquals(it, date.dayOfWeek.toWeekday())

            (0..8).forEach { assertFalse(oh.containsDateTime(date.withHourOfDay(it))) }
            (9..21).forEach { assertTrue(oh.containsDateTime(date.withHourOfDay(it))) }
            (22..23).forEach { assertFalse(oh.containsDateTime(date.withHourOfDay(it))) }
        }

        val saturday = baseDate.plusDays(SATURDAY.toInt())
        val sunday = baseDate.plusDays(SUNDAY.toInt())

        (0..8).forEach { assertFalse(oh.containsDateTime(saturday.withHourOfDay(it))) }
        (9..12).forEach { assertTrue(oh.containsDateTime(saturday.withHourOfDay(it))) }
        (13..23).forEach { assertFalse(oh.containsDateTime(saturday.withHourOfDay(it))) }
        (0..23).forEach { assertFalse(oh.containsDateTime(sunday.withHourOfDay(it))) }

    }

    @Throws(Exception::class)
    public fun testOpeningHourDescriptions() {
        val ib = locations["FBI"]!!
        checkOpeningHourDescriptions(ib.openingHours.getHoursStrings())
    }

    public fun checkOpeningHourDescriptions(strings: Map<Weekday, String>) {
        for (i in MONDAY..FRIDAY) {
            assertEquals("09:00 - 22:00", strings.get(i))
        }

        assertEquals("09:00 - 12:30", strings.get(SATURDAY))
        assertEquals(context.getString(R.string.closed), strings.get(SUNDAY))
    }

    companion object {

        private fun <K, V> newHashMap(vararg items: Any): Map<K, V> {
            return (1..items.size() step 2).toMap(
                    { items[it - 1] as K },
                    { items[it] as V }
            )
        }


        private val TOTAL_SEATS = newHashMap<String, Int>("KIT-Bibliothek Süd (Altbau)", 314, "KIT-Bibliothek Süd (Neubau)", 532, "Fachbibliothek Chemie (FBC)", 193, "Lernzentrum am Fasanenschlösschen", 94, "Wirtschaftswissenschaftliche Fakultätsbibliothek", 90, "Fachbibliothek Physik (FBP)", 86, "Informatikbibliothek", 59, "Mathematische Bibliothek", 40, "Fakultätsbibliothek Architektur", 15, "KIT-Bibliothek Nord", 37, "Fachbibliothek Hochschule Karlsruhe (FBH)", 285, "Fachbibliothek an der DHBW Karlsruhe (FBD)", 38, "TheaBib im Badischen Staatstheater", 150)
    }
}

