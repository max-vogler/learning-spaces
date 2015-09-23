package de.maxvogler.learningspaces.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.helpers.layoutInflater
import de.maxvogler.learningspaces.helpers.toWeekday
import de.maxvogler.learningspaces.models.OpeningHours
import org.jetbrains.anko.find
import org.joda.time.LocalDateTime
import java.util.*

public class OpeningHourAdapter(
        context: Context,
        private val resource: Int,
        hours: OpeningHours
) : ArrayAdapter<String>(context, resource, ArrayList(hours.getHoursStrings().values())) {

    private val weekdayNames: Array<String> = context.resources.getStringArray(R.array.weekdays)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView ?: context.layoutInflater.inflate(resource, parent, false)
        // No need to optimize with ViewHolder pattern right now:
        // OpeningHourAdapter is only used with LinearLayout,
        // not ListView


        val weekday = (position + 1).toWeekday()
        val text1 = view.find<TextView>(android.R.id.text1)
        val text2 = view.find<TextView>(android.R.id.text2)

        text1.text = weekdayNames[weekday.toInt()]
        text2.text = getItem(position)

        val style = if (weekday == LocalDateTime.now().toWeekday()) Typeface.BOLD else Typeface.NORMAL
        val tf = Typeface.create(text1.typeface, style)
        text1.typeface = tf
        text2.typeface = tf

        return view
    }

}
