package de.maxvogler.learningspaces.fragments

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.google.android.gms.maps.model.LatLng
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.squareup.otto.ThreadEnforcer
import de.maxvogler.learningspaces.activities.MainActivity
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent
import de.maxvogler.learningspaces.events.RequestLocationsEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent
import de.maxvogler.learningspaces.models.FreeSeatMeasurement
import de.maxvogler.learningspaces.models.Location
import de.maxvogler.learningspaces.models.OpeningHourPair
import de.maxvogler.learningspaces.models.Weekday
import de.maxvogler.learningspaces.services.BusProvider
import de.maxvogler.learningspaces.services.LocationService
import de.maxvogler.learningspaces.services.RememberSelectedLocationService
import org.hamcrest.Matchers.notNullValue
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class InfoFragmentTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {

    val locationService = object : LocationService() {

        init {
            val location = makeLocation()
            lastResults = mapOf(location.id to location)
        }

        @Subscribe
        override public fun onRequestLocations(e: RequestLocationsEvent)
                = bus.post(UpdateLocationsEvent(lastResults))

        override fun getLocations(): Map<String, Location>
                = lastResults

        override fun getLocations(json: String): Map<String, Location>
                = lastResults
    }

    @Before
    public override fun setUp() {
        super.setUp()

        injectInstrumentation(InstrumentationRegistry.getInstrumentation())

        BusProvider.instance = Bus(ThreadEnforcer.ANY)
        BusProvider.instance.register(locationService)
        BusProvider.instance.register(RememberSelectedLocationService())

        activity
    }

    @Test
    public fun showsData() {
        runOnMainSync {
            BusProvider.instance.post(LocationFocusChangeEvent(makeLocation()))
        }

        assertTextDisplayed("Testgebäude")
        assertTextDisplayed("100 Lernplätze insgesamt")
        assertTextDisplayed("42 frei")
        assertTextDisplayed("58 besetzt")
    }

    @Test
    public fun updatesData() {
        showsData()

        setLocationFreeSeats("TEST", 43)

        runOnMainSync {
            BusProvider.instance.post(UpdateLocationsEvent(locationService.getLocations()))
        }


        assertTextDisplayed("100 Lernplätze insgesamt")
        onView(withText("42 frei")) check doesNotExist()
        assertTextDisplayed("43 frei")
        assertTextDisplayed("57 besetzt")

    }

    @Test
    public fun updatesDataWhenReloaded() {
        showsData()

        setLocationFreeSeats("TEST", 43)

        runOnMainSync {
            instrumentation.callActivityOnPause(activity);
            instrumentation.callActivityOnStop(activity);
            instrumentation.callActivityOnRestart(activity);
            instrumentation.callActivityOnStart(activity);
            instrumentation.callActivityOnResume(activity);
        }

        instrumentation.waitForIdleSync()

        assertTextDisplayed("Testgebäude")
        assertTextDisplayed("100 Lernplätze insgesamt")
        assertTextDisplayed("43 frei")
        assertTextDisplayed("57 besetzt")
    }

    protected fun assertTextDisplayed(text: String) {
        onView(withText(text)) check matches(notNullValue())
    }

    protected fun runOnMainSync(task: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(task)
    }

    protected fun makeLocation(): Location {
        val location = Location("TEST")

        location.name = "Testgebäude"
        location.coordinates = LatLng(0.0, 0.0)
        location.measurements.add(FreeSeatMeasurement(LocalDateTime.now(), 42))
        location.totalSeats = 100
        location.openingHours.add(OpeningHourPair(Weekday.MONDAY, LocalTime(0, 0, 0), LocalTime(23, 59, 59)))

        return location
    }

    protected fun setLocationFreeSeats(id: String, freeSeats: Int) {
        val testLocation = locationService.getLocations().get(id)!!
        testLocation.measurements.clear()
        testLocation.measurements.add(FreeSeatMeasurement(LocalDateTime.now(), freeSeats))
    }

}