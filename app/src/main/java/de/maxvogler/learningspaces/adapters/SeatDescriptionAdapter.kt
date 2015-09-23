package de.maxvogler.learningspaces.adapters

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.helpers.layoutInflater
import de.maxvogler.learningspaces.models.Location
import de.maxvogler.learningspaces.views.PercentageView
import org.jetbrains.anko.find
import java.util.*

public class SeatDescriptionAdapter(
        context: Context,
        @LayoutRes private val resource: Int,
        l: Location
) : ArrayAdapter<Location>(context, resource, ArrayList(l.subLocations)) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: context.layoutInflater.inflate(resource, parent, false)

        val text1 = view.find<TextView>(R.id.name)
        val percentages = view.find<PercentageView>(R.id.percentages)
        val location = getItem(position)

        text1.text = location.name

        percentages.setTexts(R.string.closed, R.plurals.seats_free_short, R.plurals.hidden)
        percentages.values = arrayListOf(location.freeSeats, location.occupiedSeats)
        percentages.active = location.openingHours.isOpen()

        return view
    }
}
