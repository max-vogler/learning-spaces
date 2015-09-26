package de.maxvogler.learningspaces.services

import com.google.android.gms.maps.model.LatLng
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.events.RequestLocationsEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent
import de.maxvogler.learningspaces.exceptions.NetworkException
import de.maxvogler.learningspaces.helpers.*
import de.maxvogler.learningspaces.models.FreeSeatMeasurement
import de.maxvogler.learningspaces.models.Location
import de.maxvogler.learningspaces.models.OpeningHourPair
import de.maxvogler.learningspaces.services.filters.GroupKitBibSuedFilter
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.io.IOException
import java.util.*

/**
 * A network service, querying the KIT library location data.
 */
public open class LocationService : BusBasedService() {

    private val http = OkHttpClient()

    private val filters = listOf(GroupKitBibSuedFilter())

    // TODO: Initialize mLastResults with static data to show something while live data is loaded
    protected var lastResults: Map<String, Location> = emptyMap()

    private val URL = "http://services.bibliothek.kit.edu/leitsystem/getdata.php?callback=jQuery1102036302255163900554_1417122682722&location%5B0%5D=LSG%2CLST%2CLSW%2CLSN%2CLBS%2CFBC%2CLAF%2CFBW%2CFBP%2CFBI%2CFBM%2CFBA%2CBIB-N%2CFBH%2CFBD%2CTheaBib&values%5B0%5D=seatestimate%2Cmanualcount&after%5B0%5D=-10800seconds&before%5B0%5D=now&limit%5B0%5D=-17&location%5B1%5D=LSG%2CLST%2CLSW%2CLSN%2CLBS%2CFBC%2CLAF%2CFBW%2CFBP%2CFBI%2CFBM%2CFBA%2CBIB-N%2CFBH%2CFBD%2CTheaBib&values%5B1%5D=location&after%5B1%5D=&before%5B1%5D=now&limit%5B1%5D=1&refresh=&_=1417122682724"

    private val DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

    @Subscribe
    public open fun onRequestLocations(e: RequestLocationsEvent) {
        requestLocationsAsync();
    }

    public open fun requestLocationsAsync() {
        async {
            val locations = getLocations()

            uiThread {
                bus.post(UpdateLocationsEvent(locations))
            }
        }
    }

    @Throws(NetworkException::class)
    public open fun getLocations(): Map<String, Location> {
        val request = Request.Builder().url(URL).build()

        val jsonp = try {
            http.newCall(request).execute().body().string()
        } catch (e: IOException) {
            throw NetworkException(e)
        }

        val json = jsonp.substringAfter('(').substringBeforeLast(')')
        return getLocations(json)
    }

    @Throws(NetworkException::class)
    public open fun getLocations(json: String): Map<String, Location> {
        var locations: MutableMap<String, Location> = HashMap()
        val parser = JSONParser()

        try {
            parseRecursively(locations, parser.parse(json))
        } catch (e: ParseException) {
            throw NetworkException(e)
        }


        filters.forEach { locations = it.apply(locations) }
        lastResults = locations

        return locations
    }

    @Produce
    public fun getLastResults(): UpdateLocationsEvent {
        return UpdateLocationsEvent(lastResults)
    }

    private fun parseRecursively(locations: MutableMap<String, Location>, root: Any) {
        if (root is JSONObject) {
            if (root.containsKeys("location_name", "free_seats")) {
                parseFreeSeatMeasurement(locations, root)
            } else if (root.containsKeys("name", "long_name")) {
                parseLocation(locations, root)
            } else {
                root.values().forEach { parseRecursively(locations, it!!) }
            }
        } else if (root is JSONArray) {
            root.forEach { parseRecursively(locations, it!!) }
        }
    }

    private fun parseLocation(locations: MutableMap<String, Location>, node: JSONObject) {
        val name = node.string("name")!!
        val location = locations.getOrPut(name, { Location(name) })
        val coordinates = node.string("geo_coordinates")

        location.name = node.string("long_name")
        location.building = node.string("building")
        location.room = node.string("room")
        location.level = node.string("level")
        location.superLocationString = node.string("super_location")
        location.totalSeats = node.int("available_seats") ?: 0
        location.openingHours.addAll(parseOpeningHourPairs(node))

        if (coordinates != null) location.coordinates = parseLatLng(coordinates)
    }

    private fun parseOpeningHourPairs(json: JSONObject): List<OpeningHourPair> {
        val hoursArray = json.obj("opening_hours")?.array("weekly_opening_hours")!!

        return hoursArray.flatMap {
            val (open, close) = (it as JSONArray)
                    .map { it as JSONObject }
                    .map { DATE_TIME_FORMAT.parseLocalDateTime(it.string("date")) }

            val openWeekday = open.toWeekday()
            val closeWeekday = close.toWeekday()

            (openWeekday..closeWeekday).map { weekday ->
                if(openWeekday == weekday && closeWeekday == weekday) {
                    OpeningHourPair(weekday, open.toLocalTime(), close.toLocalTime())
                } else {
                    val openTime = if (openWeekday < weekday) LocalTime.MIDNIGHT else open.toLocalTime()
                    val closeTime = if(closeWeekday > weekday) LocalTime(23, 59, 59) else close.toLocalTime()

                    OpeningHourPair(weekday, openTime, closeTime)
                }
            }
        }
    }

    private fun parseFreeSeatMeasurement(locations: MutableMap<String, Location>, json: JSONObject) {
        val name = json.string("location_name")!!
        val freeSeats = json.int("free_seats")
        val date = DATE_TIME_FORMAT.parseLocalDateTime(json.obj("timestamp")?.string("date"))

        val location = locations.getOrPut(name, { Location(name) })
        if (freeSeats != null) location.measurements.add(FreeSeatMeasurement(date, freeSeats))
    }

    private fun parseLatLng(geoCoordinates: String): LatLng {
        val (lat, lng) = geoCoordinates.split(";").map { it.toDouble() }
        return LatLng(lat, lng)
    }

}