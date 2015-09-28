package de.maxvogler.learningspaces.models

import de.maxvogler.learningspaces.helpers.toWeekday
import org.joda.time.LocalDate

public enum class Weekday(val weekday: Int) {

    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    public fun toInt(): Int
            = weekday

    public fun equals(i: Int): Boolean
            = weekday == i

    public val next: Weekday
            // values() indices start by zero, so [this.weekday] returns the successor!
        get() = values().get(this.weekday % 7)

    public fun rangeTo(other: Weekday): Progression<Weekday> {
        val start = this

        return object : Progression<Weekday> {
            override val start: Weekday = start
            override val end: Weekday = other
            override val increment: Number = 1

            override fun iterator(): Iterator<Weekday> {
                val iterator = if (end >= start) {
                    IntProgressionIterator(start.toInt(), end.toInt(), 1)
                } else {
                    IntProgressionIterator(start.toInt(), end.toInt() + 7, 1)
                }

                return object : Iterator<Weekday> {
                    override fun next(): Weekday
                            = (((iterator.next() - 1 ) % 7) + 1).toWeekday()

                    override fun hasNext(): Boolean
                            = iterator.hasNext()

                }
            }

        }
    }

    companion object {
        public fun today(): Weekday =
                LocalDate.now().toWeekday()
    }

}
