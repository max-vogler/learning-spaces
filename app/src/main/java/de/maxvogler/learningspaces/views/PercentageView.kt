package de.maxvogler.learningspaces.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.PluralsRes
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.bindView
import butterknife.bindViews
import de.maxvogler.learningspaces.R

public class PercentageView : LinearLayout {

    val textInactive: TextView by bindView(R.id.inactive)

    val textViews: List<TextView> by bindViews(R.id.text1, R.id.text2)

    public var values: MutableList<Int>? = null
        set(values) {
            check(values?.size() == 2)
            $values = values
            applyValues()
        }

    private var stringResources: IntArray? = null

    public var active: Boolean = false
        set(value) {
            $active = value
            applyValues()
        }

    public constructor(context: Context) : super(context) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        orientation = LinearLayout.HORIZONTAL
        setGravity(Gravity.CENTER)

        LayoutInflater.from(context).inflate(R.layout.view_percentages, this, true)
        active = false
    }

    private fun applyValues() {
        (textInactive.layoutParams as LinearLayout.LayoutParams).weight = if (active) 0f else 1f

        if (!active) {
            textViews.forEach { (it.layoutParams as LinearLayout.LayoutParams).weight = 0f }
        } else {
            val values = values
            val stringResources = stringResources
            if (values != null) {
                val sum = values.sum()
                var percentages = values.map { 0.5f }

                if (sum != 0) {
                    percentages = values.map { it.toFloat() / sum }
                }

                if (stringResources != null) {
                    values.mapIndexed { i, it -> context.resources.getQuantityString(stringResources[i], it, it) }
                            .forEachIndexed { i, string -> textViews[i].text = string }
                }

                percentages.forEachIndexed { i, it ->
                    (textViews[i].layoutParams as LinearLayout.LayoutParams).weight = it
                }
            }
        }

        textViews.forEach { it.requestLayout() }
        textInactive.requestLayout()
    }

    public fun setTexts(@StringRes @PluralsRes inactive: Int, @PluralsRes vararg res: Int) {
        check(res.size() == 2)

        textInactive.setText(inactive)
        stringResources = res
        applyValues()
    }

}
