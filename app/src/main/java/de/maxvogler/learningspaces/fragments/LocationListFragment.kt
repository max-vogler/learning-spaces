package de.maxvogler.learningspaces.fragments

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.activities.MainActivity
import de.maxvogler.learningspaces.adapters.LocationListAdapter
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent
import de.maxvogler.learningspaces.services.BusProvider
import org.jetbrains.anko.find

/**
 * A Fragment, displaying all [Location]s in a ListView.
 */
public class LocationListFragment : ListFragment() {

    private var adapter: LocationListAdapter
        get() = listAdapter as LocationListAdapter
        set(adapter: LocationListAdapter) {
            listAdapter = adapter
        }

    private val bus = BusProvider.instance

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_list, container, false)
        val menu = layout.find<Toolbar>(R.id.toolbar)

        menu.title = activity.title
        menu.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        menu.setNavigationOnClickListener { mainActivity.animateListFragmentVisibility(false) }

        if(mainActivity.isTranslucentStatusBar()) {
            (menu.layoutParams as ViewGroup.MarginLayoutParams).topMargin = mainActivity.calculateStatusBarHeight()
        }

        return layout
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }


    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        bus.post(LocationFocusChangeEvent(adapter.getItem(position), animateMap = true))
    }

    @Subscribe
    public fun onUpdateLocations(event: UpdateLocationsEvent) {
        adapter = LocationListAdapter(activity, event.locations.map { it.getValue() })
    }

    companion object {
        public fun newInstance(): LocationListFragment = LocationListFragment()
    }
}