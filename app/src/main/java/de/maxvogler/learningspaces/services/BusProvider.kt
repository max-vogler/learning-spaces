package de.maxvogler.learningspaces.services

import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer


public object BusProvider {

    public var instance: Bus = Bus()

}