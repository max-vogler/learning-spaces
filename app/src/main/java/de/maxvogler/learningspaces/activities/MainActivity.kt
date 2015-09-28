package de.maxvogler.learningspaces.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import butterknife.bindView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.ANCHORED
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.HIDDEN
import com.squareup.otto.Subscribe
import de.maxvogler.learningspaces.R
import de.maxvogler.learningspaces.events.LocationFocusChangeEvent
import de.maxvogler.learningspaces.events.PanelVisibilityChangedEvent
import de.maxvogler.learningspaces.events.RequestLocationsEvent
import de.maxvogler.learningspaces.events.UpdateLocationsEvent
import de.maxvogler.learningspaces.helpers.itemsSequence
import de.maxvogler.learningspaces.helpers.tint
import de.maxvogler.learningspaces.services.BusProvider
import org.jetbrains.anko.browse
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

/**
 * The main activity. It contains a [SlidingUpPanelLayout], allowing to drag the
 * [LocationInfoFragment] over the [LocationMapFragment]. On the top of the screen, a [Toolbar] is
 * shown. Pressing the hamburger menu button on it, reveals all Locations in the [LocationListFragment].
 */
public class MainActivity : AppCompatActivity() {

    private val toolbar: Toolbar by bindView(R.id.toolbar)

    private val toolbarContainer: CardView by bindView(R.id.container_toolbar)

    private val slidingPanelLayout: SlidingUpPanelLayout by bindView(R.id.layout)

    private val dragHandle: View by bindView(R.id.drag_handle)

    private val listLayout: ViewGroup by bindView(R.id.list)

    private val infoFragment: View by bindView(R.id.info)

    private val bus = BusProvider.instance

    private var refreshMenuItem: MenuItem? = null

    private val menuButtonPosition: IntArray = intArrayOf(0, 0)

    @Suppress("DEPRECATED_SYMBOL_WITH_MESSAGE")
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)

        // Initialize the main Toolbar
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        val menuIcon = resources.getDrawable(R.drawable.ic_menu_white_24dp, theme)
        menuIcon.tint(resources.getColor(R.color.primary))
        supportActionBar.setHomeAsUpIndicator(menuIcon)

        // Add a proper top margin, if the system supports a translucent status bar
        if (isTranslucentStatusBar()) {
            val statusBarHeight = calculateStatusBarHeight()
            (toolbarContainer.layoutParams as ViewGroup.MarginLayoutParams).topMargin += statusBarHeight
            (infoFragment.layoutParams as ViewGroup.MarginLayoutParams).topMargin += statusBarHeight

            toolbarContainer.requestLayout()
            listLayout.getChildAt(0).requestLayout()
            infoFragment.requestLayout()
        }

        // Initialize the SlidingPanelLayout, containing the LocationMapFragment and LocationInfoFragment
        slidingPanelLayout.setPanelSlideListener(panelSlideListener)
        slidingPanelLayout.setDragView(dragHandle)
        slidingPanelLayout.panelState = HIDDEN
        slidingPanelLayout.requestLayout()
    }

    public fun calculateStatusBarHeight(): Int
            = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

    public fun isTranslucentStatusBar(): Boolean
            = (window.attributes.flags and FLAG_TRANSLUCENT_STATUS) != 0

    override fun onResume() {
        super.onResume()
        bus.register(this)

        // Always reload the locations, when (re-)entering the App
        bus.post(RequestLocationsEvent())
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    @Suppress("DEPRECATED_SYMBOL_WITH_MESSAGE")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        refreshMenuItem = menu.findItem(R.id.refresh)

        // Tint all menu icons with the primary color
        val color = resources.getColor(R.color.primary)
        menu.itemsSequence().forEach { it.icon?.tint(color) }

        var globalLayoutListener: () -> Unit
        globalLayoutListener = {
            // Prevent the InfoFragment from overlapping the Toolbar
            (infoFragment.layoutParams as ViewGroup.MarginLayoutParams).topMargin += toolbarContainer.height + dip(16)

            // Save the position of the menu button, to use it as center for the circular reveal
            val hamburgerMenuIcon = toolbar.childrenSequence().first { it is ImageButton }
            hamburgerMenuIcon.getLocationOnScreen(menuButtonPosition)
            menuButtonPosition[0] += hamburgerMenuIcon.width / 2
            menuButtonPosition[1] += hamburgerMenuIcon.width / 2

            slidingPanelLayout.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener);
        }

        slidingPanelLayout.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.refresh -> bus.post(RequestLocationsEvent())

            R.id.open_website -> browse(getString(R.string.website_url));

            R.id.open_about -> startActivity<AboutActivity>();

        // Pressing the hamburger menu button toggles the LocationListFragment visibility
            android.R.id.home -> animateListFragmentVisibility(!isListVisible())

            else -> return super.onOptionsItemSelected(item)

        }

        return true
    }

    /**
     * Sets the visibility of the [LocationListFragment]. A circular reveal animation shows
     * or hides the list.
     *
     * When the [LocationListFragment] is fully visible, the [SlidingUpPanelLayout] and
     * [LocationMapFragment] are hidden to improve rendering performance.
     *
     * @param listVisible
     */
    public fun animateListFragmentVisibility(listVisible: Boolean) {
        // On quick successive presses, do nothing
        if (isListVisible() == listVisible)
            return;

        val max = Math.max(listLayout.height, listLayout.width).toFloat()

        if (listVisible)
            listLayout.visibility = VISIBLE
        else
            slidingPanelLayout.visibility = VISIBLE

        // This should not be necessary, because toggles faster than the animation are prevented
        listLayout.clearAnimation()

        // Create a circular reveal animation, centered on the hambuger menu icon
        val anim = ViewAnimationUtils.createCircularReveal(listLayout,
                menuButtonPosition[0],
                menuButtonPosition[1],
                if (listVisible) 0f else max,
                if (listVisible) max else 0f
        )

        // Interpolate the animation around the icon smoothly
        anim.interpolator = if (listVisible) AccelerateInterpolator() else DecelerateInterpolator()

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!listVisible)
                    listLayout.visibility = INVISIBLE
                else
                    slidingPanelLayout.visibility = INVISIBLE
            }
        })

        anim.start()
    }

    private fun isListVisible(): Boolean
            = listLayout.visibility == VISIBLE

    @Subscribe
    public fun onReceiveLocations(event: UpdateLocationsEvent) {
        setRefreshProgressVisible(false)
    }

    @Subscribe
    public fun onLocationsRequested(event: RequestLocationsEvent) {
        setRefreshProgressVisible(true)
    }

    @Subscribe
    public fun onLocationFocusChange(event: LocationFocusChangeEvent) {
        if (event.hasSelection()) {
            // If the user selects a Location in the LocationListFragment, hide it and show the map
            animateListFragmentVisibility(false)
            slidingPanelLayout.panelState = COLLAPSED
        } else {
            // If the user discards the selected location, hide the LocationInfoFragment
            slidingPanelLayout.panelState = HIDDEN
        }
    }

    private fun setRefreshProgressVisible(refreshing: Boolean) {
        if (refreshMenuItem == null)
            return

        if (refreshing) {
            MenuItemCompat.setActionView(refreshMenuItem, R.layout.view_appbar_progress)
        } else {
            MenuItemCompat.setActionView(refreshMenuItem, null)
        }
    }

    override fun onBackPressed() {
        if (isListVisible()) {
            // Pressing back when viewing the LocationListFragment hides it and shows the map
            animateListFragmentVisibility(false)
        } else if (slidingPanelLayout.panelState == EXPANDED || slidingPanelLayout.panelState == ANCHORED) {
            // Pressing back with the LocationInfoFragment open, minimizes it
            slidingPanelLayout.panelState = COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    private val panelSlideListener = object : SlidingUpPanelLayout.SimplePanelSlideListener() {
        override fun onPanelSlide(view: View?, v: Float) {
            // Legacy code, to fade out the Toolbar when swiping up the SlidingUpPanelLayout
            //toolbarContainer.setVisibility(if (v < 1f) View.VISIBLE else View.INVISIBLE);
            //toolbarContainer.setAlpha(1 - v)
        }

        override fun onPanelCollapsed(view: View?) = bus.post(PanelVisibilityChangedEvent(false))

        override fun onPanelExpanded(view: View?) = bus.post(PanelVisibilityChangedEvent(true))

        override fun onPanelAnchored(view: View?) = bus.post(PanelVisibilityChangedEvent(true))

        override fun onPanelHidden(view: View?) = bus.post(PanelVisibilityChangedEvent(false))
    }
}
