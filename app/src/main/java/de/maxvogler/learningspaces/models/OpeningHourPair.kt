package de.maxvogler.learningspaces.models


import org.joda.time.Interval
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

public class OpeningHourPair(
        public val weekday: Weekday,
        public val open: LocalTime,
        public val close: LocalTime
) : Comparable<OpeningHourPair> {

    public val interval = Interval(open.toDateTimeToday(), close.toDateTimeToday())

    public fun contains(weekday: Weekday, time: LocalTime): Boolean {
        return weekday == this.weekday && interval.contains(time.toDateTimeToday());
    }

    public fun contains(weekday: Weekday): Boolean {
        return weekday == this.weekday && close != LocalTime.MIDNIGHT
    }

    override fun toString(): String {
        return open.toString(format) + " - " + close.toString(format)
    }

    override fun compareTo(other: OpeningHourPair): Int {
        return compareValuesBy(this, other,
                { it.weekday.toInt() },
                { it.open },
                { it.close })
    }

    companion object {
        private val format = DateTimeFormat.forPattern("HH:mm")
    }

}
