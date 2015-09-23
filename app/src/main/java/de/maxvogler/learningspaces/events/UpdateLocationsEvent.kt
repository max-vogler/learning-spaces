package de.maxvogler.learningspaces.events


import de.maxvogler.learningspaces.models.Location

public data class UpdateLocationsEvent(public val locations: Map<String, Location>) {

}
