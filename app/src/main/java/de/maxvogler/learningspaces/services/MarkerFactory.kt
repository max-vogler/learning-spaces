package de.maxvogler.learningspaces.services

import android.content.Context
import android.graphics.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.models.Location

public class MarkerFactory(private val context: Context) {

    public fun createMarker(location: Location): MarkerOptions {
        if (location.coordinates == null) {
            throw IllegalArgumentException("Cannot create marker without position for " + location)
        }

        return MarkerOptions()
                .position(location.coordinates)
                .icon(BitmapDescriptorFactory.fromBitmap(createIcon(location)))
                .flat(true)
                .anchor(0.5f, 0.5f)
    }

    public fun createSelectedMarker(location: Location): MarkerOptions {
        return MarkerOptions().position(location.coordinates)
    }

    public fun createIcon(location: Location): Bitmap {
        val o = BitmapFactory.Options()
        o.inMutable = true

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_local_library_black_24dp, o)

        val color = if (location.openingHours.isClosed()) {
            context.resources.getColor(R.color.location_unavailable)
        } else {
            getColor(location.freeSeatsPercentage)
        }

        val paint = Paint()
        paint.setColorFilter(LightingColorFilter(0, color))
        Canvas(bitmap).drawBitmap(bitmap, 0f, 0f, paint)

        return bitmap
    }

    private fun getColor(percentage: Float): Int {
        if (percentage > 0.4) {
            return context.resources.getColor(R.color.location_good)
        } else if (percentage > 0.2) {
            return context.resources.getColor(R.color.location_okay)
        } else {
            return context.resources.getColor(R.color.location_bad)
        }
    }

}
