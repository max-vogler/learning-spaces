package de.maxvogler.learningspaces.events

import de.maxvogler.learningspaces.models.Location

public data class LocationFocusChangeEvent(
        public val location: Location?,
        public var animateMap: Boolean = false
) {

    public fun hasSelection(): Boolean = location != null

}