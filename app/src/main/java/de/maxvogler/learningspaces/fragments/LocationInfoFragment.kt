package de.maxvogler.learningspaces.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.adapters.OpeningHourAdapter
import de.maxvogler.learningspaces.adapters.SeatDescriptionAdapter
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent
import de.maxvogler.learningspaces.helpers.fillWithAdapter
import de.maxvogler.learningspaces.models.Location
import de.maxvogler.learningspaces.services.BusProvider
import de.maxvogler.learningspaces.views.PercentageView

/**
 * A Fragment, displaying detailed information about the currently selected [Location]
 */
public class LocationInfoFragment : Fragment() {

    private val nameText: TextView by bindView(R.id.name)
    private val buildingText: TextView by bindView(R.id.descriptionBuilding)
    private val totalSeatsText: TextView by bindView(R.id.descriptionSeatsTotal)
    private val hoursLayout: ViewGroup by bindView(R.id.hours)
    private val seatsLayout: ViewGroup by bindView(R.id.seats)
    private val seatsContainer: ViewGroup by bindView(R.id.seatsContainer)
    private val seatsPercentages: PercentageView by bindView(R.id.seatsOverview)

    private var location: Location? = null

    private val mBus = BusProvider.instance

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_info, container, false)

    override fun onResume() {
        super.onResume()
        mBus.register(this)
    }

    override fun onPause() {
        mBus.unregister(this)
        super.onPause()
    }


    @Subscribe
    public fun onLocationFocusChange(event: LocationFocusChangeEvent) {
        // Only update this Fragment, if a new Location is set. If no Location is selected,
        // the Fragment will be hidden anyway.
        if (event.location != null) {
            location = event.location
            updateInformation(event.location)
        }
    }

    @Subscribe
    public fun onUpdateLocations(event: UpdateLocationsEvent) {
        var oldLocation = location

        if (oldLocation != null) {
            // TODO: Hide InfoFragment or display error if location == null
            location = event.locations.get(oldLocation.id)
            updateInformation(location!!)
        }
    }

    private fun updateInformation(location: Location) {
        nameText.text = location.name

        seatsPercentages.setTexts(R.string.closed, R.plurals.seats_free_short, R.plurals.hidden)
        seatsPercentages.values = arrayListOf(location.freeSeats, location.occupiedSeats)
        seatsPercentages.active = location.openingHours.isOpen()

        totalSeatsText.text = resources.getQuantityString(R.plurals.seats_total_long, location.totalSeats, location.totalSeats)

        hoursLayout.fillWithAdapter(OpeningHourAdapter(activity, R.layout.view_open_hours, location.openingHours))

        val seatsAdapter = SeatDescriptionAdapter(activity, R.layout.view_seat_count, location)
        seatsLayout.fillWithAdapter(seatsAdapter)
        seatsContainer.visibility = if (seatsAdapter.isEmpty) View.GONE else View.VISIBLE

        buildingText.text = getString(R.string.building, location.building)
        buildingText.visibility = if (location.building == null) View.GONE else View.VISIBLE
    }

    companion object {
        public fun newInstance(): LocationInfoFragment = LocationInfoFragment()
    }
}
