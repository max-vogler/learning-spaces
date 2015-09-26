package de.maxvogler.learningspaces.models

import android.support.test.runner.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
public class LocationTest {

    private fun makeLocation(): Location {
        val location = Location("TEST")

        location.name = "Testgebäude"
        location.coordinates = LatLng(0.0, 0.0)
        location.measurements.add(FreeSeatMeasurement(LocalDateTime.now(), 42))
        location.totalSeats = 100
        location.openingHours.add(OpeningHourPair(Weekday.MONDAY, LocalTime(0, 0, 0), LocalTime(23, 59, 59)))

        return location
    }

    @Test
    public fun calculatesSeatsCorrectly() {
        val location = makeLocation()

        assertEquals(location.freeSeats, 42)
        assertEquals(location.occupiedSeats, 58)
        assertEquals(location.totalSeats, 100)
    }

    @Test
    public fun updatesSeatsCorrectly() {
        val location = makeLocation()

        location.measurements.clear()
        assertNull(location.lastMeasurement())

        location.measurements.add(FreeSeatMeasurement(LocalDateTime.now(), 43))
        assertEquals(location.freeSeats, 43)
        assertEquals(location.occupiedSeats, 57)
        assertEquals(location.totalSeats, 100)
    }


}