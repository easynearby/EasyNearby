package com.changeworld.android_easynearby

import com.changeworld.easynearby.ConnectionStrategy
import com.google.android.gms.nearby.connection.Strategy

internal fun ConnectionStrategy.toStrategy():Strategy = when (this) {
    ConnectionStrategy.CLUSTER -> Strategy.P2P_CLUSTER
    ConnectionStrategy.POINT_TO_POINT -> Strategy.P2P_POINT_TO_POINT
    ConnectionStrategy.STAR -> Strategy.P2P_STAR
}