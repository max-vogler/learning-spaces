package de.maxvogler.learningspaces.services

import com.squareup.otto.Bus


public open class BusBasedService {

    public val bus: Bus = BusProvider.instance

    public open fun start() {
        bus.register(this)
    }

    public open fun stop() {
        bus.unregister(this)
    }
}
