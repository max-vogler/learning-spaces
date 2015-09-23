package de.maxvogler.learningspaces.services

import android.util.Log
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent
import de.maxvogler.learningspaces.events.PanelVisibilityChangedEvent
import de.maxvogler.learningspaces.events.RequestLocationsEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent

public class BusEventLogger : BusBasedService() {

    protected fun logEvent(o: Any) {
        Log.i("BusEventLogger", o.toString())
    }

    @Subscribe
    public fun log(event: LocationFocusChangeEvent): Unit = logEvent(event)

    @Subscribe
    public fun log(event: RequestLocationsEvent): Unit = logEvent(event)

    @Subscribe
    public fun log(event: UpdateLocationsEvent): Unit = logEvent(event)

    @Subscribe
    public fun log(event: PanelVisibilityChangedEvent): Unit = logEvent(event)

}
