package de.maxvogler.learningspaces.models


import de.maxvogler.learningspaces.helpers.toWeekday
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

public class OpeningHourPair(
        public val open: LocalDateTime,
        public val close: LocalDateTime
) : Comparable<OpeningHourPair> {

    init {
        check(open <= close)
    }

    public fun contains(dateTime: LocalDateTime): Boolean {
        val day = dateTime.toWeekday()
        val openDay = open.toWeekday()
        val closeDay = close.toWeekday()

        val time = dateTime.toLocalTime()
        val openTime = open.toLocalTime()
        val closeTime = close.toLocalTime()

        var isContained = true

        if (day < openDay) {
            isContained = false
        } else if (day > closeDay) {
            isContained = false
        } else if (day == openDay && (time <= openTime || time == openTime)) {
            isContained = false
        } else if (day == closeDay && time > closeTime || time == closeTime) {
            isContained = false
        }

        return isContained
    }

    public fun containsWeekday(weekday: Weekday): Boolean {
        val openDay = open.toWeekday()
        val closeDay = close.toWeekday()

        return openDay <= weekday && closeDay >= weekday && !(closeDay == weekday && close.toLocalTime() == LocalTime.MIDNIGHT)
    }


    public fun toTimeString(currentDay: Weekday): String {
        val str = StringBuilder(13)

        val openWeekday = open.toWeekday()
        val closeWeekday = close.toWeekday()
        val openTime = open.toLocalTime()
        val closeTime = close.toLocalTime()

        if (containsWeekday(currentDay)) {

            if (openWeekday < currentDay) {
                // opening time started already on a day before
                str.append("00:00")
            } else if (currentDay == openWeekday) {
                // opens on the current day
                str.append(openTime.toString(format))
            } else {
                // does not open on the current day => should not happen!
                throw IllegalArgumentException()
            }

            str.append(" - ")

            if (closeWeekday > currentDay) {
                // closes on a day after the current day
                str.append("24:00")
            } else if (currentDay == closeWeekday) {
                // closes on the current day

                if (closeTime.hourOfDay == 23 && closeTime.minuteOfHour == 59) {
                    str.append("24:00")
                } else {
                    str.append(closeTime.toString(format))
                }
            } else {
                // closes already on a day before the current day
                throw IllegalArgumentException()
            }
        }

        return str.toString()
    }

    override fun compareTo(other: OpeningHourPair): Int {
        return open.compareTo(other.open)
    }

    companion object {
        private val format = DateTimeFormat.forPattern("HH:mm")
    }

}
