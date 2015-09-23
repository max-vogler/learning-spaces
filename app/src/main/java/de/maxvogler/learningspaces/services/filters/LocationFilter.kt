package de.maxvogler.learningspaces.services.filters

import de.maxvogler.learningspaces.models.Location

public abstract class LocationFilter {

    public abstract fun apply(locations: MutableMap<String, Location>): MutableMap<String, Location>

}
