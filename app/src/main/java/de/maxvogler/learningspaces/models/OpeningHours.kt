package de.maxvogler.learningspaces.models


import org.joda.time.LocalDateTime
import java.util.*

public class OpeningHours : TreeSet<OpeningHourPair>() {

    public fun containsDateTime(dateTime: LocalDateTime): Boolean {
        return any { it.contains(dateTime) }
    }

    public fun getHoursForToday(): SortedSet<OpeningHourPair> {
        return getHoursForDay(Weekday.today())
    }

    public fun getHoursForDay(weekday: Weekday): SortedSet<OpeningHourPair> {
        return filterTo(TreeSet<OpeningHourPair>(), { it.containsWeekday(weekday) })
    }

    public fun getHoursForDayString(weekday: Weekday): String {
        val hours = getHoursForDay(weekday)

        if (hours.isEmpty()) {
            return "Geschlossen"
        } else {
            return hours.map { it.toTimeString(weekday) }.sorted().joinToString(separator = ", ")
        }
    }


    public fun getHoursStrings(): Map<Weekday, String> {
        val strings = TreeMap<Weekday, String>()
        Weekday.values().forEach { strings.put(it, getHoursForDayString(it)) }
        return strings
    }

    public fun isOpen(): Boolean {
        return containsDateTime(LocalDateTime.now())
    }

    public fun isClosed(): Boolean {
        return !isOpen()
    }

}
