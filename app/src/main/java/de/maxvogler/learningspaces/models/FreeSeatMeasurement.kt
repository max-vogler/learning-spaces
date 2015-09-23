package de.maxvogler.learningspaces.models

import org.joda.time.LocalDateTime

public data class FreeSeatMeasurement(
        public val date: LocalDateTime,
        public val freeSeats: Int
) : Comparable<FreeSeatMeasurement> {

    override fun compareTo(other: FreeSeatMeasurement): Int
            = date.compareTo(other.date)
}
