package de.maxvogler.learningspaces.services

import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent

public class RememberSelectedLocationService : BusBasedService() {

    public var lastLocationSelectedEvent: LocationFocusChangeEvent = LocationFocusChangeEvent(null)
        @Produce get
        @Subscribe set

}
