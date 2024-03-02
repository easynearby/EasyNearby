package com.changeworld.easynearby.advertising

import com.changeworld.easynearby.ConnectionStrategy

data class DeviceInfo(
    /**
     * A human readable name for this endpoint, to appear on the remote device. Defined by client/application.
     */
    val name: String,
    /**
     * An identifier to advertise your app to other endpoints. This can be an arbitrary string, so long as it uniquely identifies your service. A good default is to use your app's package name.
     */
    val serviceId: String,
    val strategy: ConnectionStrategy,
)