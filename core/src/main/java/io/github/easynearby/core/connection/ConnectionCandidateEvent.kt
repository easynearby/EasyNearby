package io.github.easynearby.core.connection

/**
 * Connection candidate event that is emitted when a connection candidate is discovered
 * or lost by both [AdvertiseManager] and [DiscoveryManager].
 * @param type [ConnectionEventType]
 * @param candidate [ConnectionCandidate]
 */
data class ConnectionCandidateEvent(
    val type: ConnectionEventType,
    val candidate: ConnectionCandidate
)