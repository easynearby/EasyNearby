package com.changeworld.easynearby.connection

data class ConnectionCandidateEvent(
    val type: ConnectionEventType,
    val candidate: ConnectionCandidate
)