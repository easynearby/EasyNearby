package com.changeworld.android_easynearby.impl

import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution

internal sealed class ConnectionEvents {
    class ConnectionInitiated(val endpoint:String, val connectionInfo: ConnectionInfo): ConnectionEvents()
    class ConnectionResult(val endpoint: String, val result: ConnectionResolution): ConnectionEvents()
    class Disconnected(val endpoint: String) : ConnectionEvents()
}