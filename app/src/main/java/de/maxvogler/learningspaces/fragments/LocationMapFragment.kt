package de.maxvogler.learningspaces.fragments

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent
import de.maxvogler.learningspaces.events.PanelVisibilityChangedEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent
import de.maxvogler.learningspaces.models.Location
import de.maxvogler.learningspaces.services.BusProvider
import de.maxvogler.learningspaces.services.MarkerFactory
import kotlin.properties.Delegates

/**
 * A Fragment, displaying all [Location]s in a [GoogleMap].
 */
public class LocationMapFragment : SupportMapFragment() {

    private val bus = BusProvider.instance

    private var markers: Map<Location, Marker> = emptyMap()

    private var markerFactory: MarkerFactory by Delegates.notNull()

    private var selectedMarker: Marker? = null

    private var selectedLocation: Location? = null

    private var mapView: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        markerFactory = MarkerFactory(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getMapAsync {
            this.mapView = it
            val pos = LatLng(49.011019, 8.414874)
            it.uiSettings.isZoomControlsEnabled = false
            it.uiSettings.isMyLocationButtonEnabled = false
            it.uiSettings.isCompassEnabled = false
            it.isMyLocationEnabled = true
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f))

            it.setOnMarkerClickListener { marker ->
                val location = markers.entrySet().firstOrNull { it.getValue() == marker }?.key
                if (location != null) {
                    showSelectedMarker(markerFactory.createSelectedMarker(location))
                    bus.post(LocationFocusChangeEvent(location))
                }
                true
            }

            it.setOnMapClickListener {
                bus post LocationFocusChangeEvent(null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        bus.unregister(this)
        super.onPause()
    }


    @Subscribe
    public fun onReceiveLocations(event: UpdateLocationsEvent) {
        val locations = event.locations

        removeSelectedMarker()
        mapView?.clear()

        markers = locations.values().toMap({ it }, { it })
                .mapValues { markerFactory.createMarker(it.getValue()) }
                .mapValues { mapView?.addMarker(it.getValue()) }
                .filter { it.getValue() != null }
                .mapValues { it.getValue()!! }

        val oldSelected = selectedLocation
        if (oldSelected != null) {
            val newSelected = locations.get(oldSelected.id)

            if (newSelected != null) {
                selectedLocation = newSelected
                showSelectedMarker(markerFactory.createSelectedMarker(newSelected))
            }
        }
    }

    @Subscribe
    public fun onLocationFocusChange(event: LocationFocusChangeEvent) {
        removeSelectedMarker()
        selectedLocation = event.location

        if (event.location != null) {
            val marker = markerFactory.createSelectedMarker(event.location)
            showSelectedMarker(marker)

            if (event.animateMap) {
                mapView?.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            }
        }
    }


    @Subscribe
    public fun onPanelVisibilityChanged(event: PanelVisibilityChangedEvent) {
        mapView?.uiSettings?.setAllGesturesEnabled(!event.visible)
    }

    private fun removeSelectedMarker() {
        selectedMarker?.remove()
        selectedMarker = null
    }

    private fun showSelectedMarker(options: MarkerOptions) {
        removeSelectedMarker()
        selectedMarker = mapView?.addMarker(options)
    }
}
