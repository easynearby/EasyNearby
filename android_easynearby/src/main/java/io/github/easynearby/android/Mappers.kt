package io.github.easynearby.android

import com.google.android.gms.nearby.connection.Strategy
import io.github.easynearby.core.ConnectionStrategy

internal fun ConnectionStrategy.toStrategy(): Strategy = when (this) {
    ConnectionStrategy.CLUSTER -> Strategy.P2P_CLUSTER
    ConnectionStrategy.POINT_TO_POINT -> Strategy.P2P_POINT_TO_POINT
    ConnectionStrategy.STAR -> Strategy.P2P_STAR
}