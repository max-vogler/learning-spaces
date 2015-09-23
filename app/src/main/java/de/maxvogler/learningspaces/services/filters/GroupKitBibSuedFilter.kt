package de.maxvogler.learningspaces.services.filters

import de.maxvogler.learningspaces.models.Location
import java.util.*

public class GroupKitBibSuedFilter : LocationFilter() {

    override fun apply(locations: MutableMap<String, Location>): MutableMap<String, Location> {
        val ret = group(group(locations, "KITBIBS_N"), "KITBIBS_A")

        val bibA = ret.get("KITBIBS_A")
        bibA?.name = "KIT-Bibliothek Süd (Altbau)"

        val bibN = ret.get("KITBIBS_N")
        bibN?.name = "KIT-Bibliothek Süd (Neubau)"

        bibA?.subLocations?.forEach { it.name = makeLevelName(it) }
        bibN?.subLocations?.forEach { it.name = makeLevelName(it) }

        return ret
    }

    protected fun makeLevelName(l: Location): String {
        return "${l.level}. OG"
    }

    protected fun group(locations: Map<String, Location>, id: String): MutableMap<String, Location> {
        val ret = HashMap<String, Location>()
        locations.filterTo(ret, { id != it.value.superLocationString })

        val subLocations = locations.filter { id == it.value.superLocationString }.values()

        val superLocation = ret.getOrPut(id, {
            val superLocation = Location(id)

            if (superLocation.coordinates == null) {
                superLocation.coordinates = subLocations.first { it.coordinates != null }.coordinates
            }

            if (superLocation.building == null) {
                superLocation.building = subLocations.first { it.building != null }.building
            }

            if (superLocation.openingHours.isEmpty()) {
                superLocation.openingHours.addAll(subLocations.first { it.openingHours.isNotEmpty() }.openingHours)
            }

            superLocation
        })

        superLocation.subLocations.addAll(subLocations)

        return ret
    }
}
