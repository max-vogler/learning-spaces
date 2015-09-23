package de.maxvogler.learningspaces.adapters

import android.widget.TextView
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.helpers.toWeekday
import de.maxvogler.learningspaces.services.LocationServiceBaseTest
import org.jetbrains.anko.find
import kotlin.test.assertEquals

public class OpeningHourAdapterTest : LocationServiceBaseTest() {

    @Throws(Exception::class)
    public fun testItems() {
        val oh = locations.get("FBI")!!.openingHours
        val adapter = OpeningHourAdapter(context, R.layout.view_open_hours, oh)

        val strings = (1..adapter.count).toMap(
                { it.toWeekday() },
                { adapter.getItem(it - 1) }
        )

        checkOpeningHourDescriptions(strings)
    }

    @Throws(Exception::class)
    public fun testTimeLabels() {
        val oh = locations["FBI"]!!.openingHours
        val adapter = OpeningHourAdapter(context, R.layout.view_open_hours, oh)

        assertEquals(7, adapter.count)

        val strings = (1..adapter.count).toMap(
                { it.toWeekday() },
                { adapter.getView(it - 1, null, null).find<TextView>(android.R.id.text2).text.toString() }
        )

        checkOpeningHourDescriptions(strings)
    }

}
