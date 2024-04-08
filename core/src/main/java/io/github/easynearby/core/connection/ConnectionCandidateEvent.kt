package io.github.easynearby.core.connection

data class ConnectionCandidateEvent(
    val type: ConnectionEventType,
    val candidate: ConnectionCandidate
)