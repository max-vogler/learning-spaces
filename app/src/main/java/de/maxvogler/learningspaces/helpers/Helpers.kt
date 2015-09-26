package de.maxvogler.learningspaces.helpers

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Adapter
import de.maxvogler.learningspaces.models.Weekday
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.json.simple.JSONArray
import org.json.simple.JSONObject

public fun ViewGroup.fillWithAdapter(adapter: Adapter) {
    removeAllViews()
    (0..adapter.count - 1).forEach { addView(adapter.getView(it, null, this)) }
}

fun JSONObject.string(key: Any): String? = this[key] as String?

fun JSONObject.int(key: Any): Int? = (this[key] as Long?)?.toInt()

fun JSONObject.obj(key: Any): JSONObject? = this[key] as JSONObject?

fun JSONObject.array(key: Any): JSONArray? = this[key] as JSONArray?

fun <K, V> Map<K, V>.containsKeys(vararg keys: Any): Boolean = keys.all { this.containsKey(it) }

fun Int.toWeekday(): Weekday = Weekday.values()[this - 1]

fun LocalDate.toWeekday(): Weekday = this.dayOfWeek.toWeekday()

fun LocalDateTime.toWeekday(): Weekday = this.dayOfWeek.toWeekday()

fun Drawable.tint(primaryColor: Int) = this.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)
