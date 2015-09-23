package de.maxvogler.learningspaces.models


import com.google.android.gms.maps.model.LatLng
import java.util.*

public data class Location(public val id: String) {

    /**
     * The full name, e.g. "KIT Bibliothek Süd"
     */
    public var name: String? = null

    /**
     * The building, e.g. 30.51
     */
    public var building: String? = null

    /**
     * The level, commonly used for [subLocations]
     */
    public var level: String? = null

    /**
     * The room no., commonly used for [subLocations]
     */
    public var room: String? = null

    /**
     * The geographic location (latitude, longitude)
     */
    public var coordinates: LatLng? = null

    /**
     * The total number of seats, either set explicitly or calculated by summing up the [totalSeats] of
     * all currently open [subLocations].
     */
    public var totalSeats: Int = 0
        get() {
            return if ($totalSeats != 0)
                $totalSeats
            else
                subLocations.filter { it.openingHours.isOpen() }.sumBy { it.totalSeats }
        }

    /**
     * The total number of seats, either derived by [lastMeasurement], or by summing up the [freeSeats]
     * of all currently open [subLocations].
     */
    public val freeSeats: Int
        get() = lastMeasurement()?.freeSeats ?:
                subLocations filter { it.openingHours.isOpen() } sumBy { it.freeSeats }

    /**
     * The percentage of free seats (is displayed as 0.5f, when [totalSeats] is 0)
     */
    public val freeSeatsPercentage: Float
        get() = if (totalSeats == 0) 0.5f else freeSeats.toFloat() / totalSeats

    /**
     * The number of occupied seats, derived by calculating [totalSeats] - [freeSeats]
     */
    public val occupiedSeats: Int
        get() = totalSeats - freeSeats

    /**
     * The percentage of occupied seats (is displayed as 0.5f, when [totalSeats] is 0)
     */
    public val occupiedSeatsPercentage: Float
        get() = 1 - freeSeatsPercentage

    /**
     * the superior location (e.g. floors may be associated to a building)
     */
    public var superLocation: Location? = null

    public var superLocationString: String? = null

    /**
     * the sub locations (e.g. a building may have many floors)
     */
    public var subLocations: MutableSet<Location> = HashSet()

    public var measurements: SortedSet<FreeSeatMeasurement> = TreeSet()

    public val openingHours: OpeningHours = OpeningHours()

    public fun lastMeasurement(): FreeSeatMeasurement?
            = measurements.lastOrNull()
}
