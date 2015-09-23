package de.maxvogler.learningspaces.adapters

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.helpers.layoutInflater
import de.maxvogler.learningspaces.models.Location
import de.maxvogler.learningspaces.models.Weekday
import org.jetbrains.anko.find

public class LocationListAdapter(context: Context, locations: List<Location>)
: ArrayAdapter<Location>(context, LocationListAdapter.layout, locations) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: context.layoutInflater.inflate(layout, parent, false)

        val freeSeats = view.find<TextView>(R.id.seats_free)
        val occupiedSeats = view.find<TextView>(R.id.seats_occupied)
        val openingHours = view.find<TextView>(R.id.time)
        val location = getItem(position)

        view.find<TextView>(R.id.name).text = location.name!!

        freeSeats.text = location.freeSeats.toString()
        occupiedSeats.text = location.occupiedSeats.toString()

        freeSeats.visibility = if (location.openingHours.isOpen()) VISIBLE else GONE
        occupiedSeats.visibility = if (location.openingHours.isOpen()) VISIBLE else GONE

        openingHours.text = location.openingHours.getHoursForDayString(Weekday.today())

        return view
    }

    companion object {
        private val layout = R.layout.view_location_list_item
    }
}
