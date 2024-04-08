package io.github.easynearby.core.connection

/**
 * Connection event type for [ConnectionCandidateEvent].
 * If the event is [DISCOVERED] the candidate is added to the list of discovered candidates.
 * If the event is [LOST] the candidate is removed from the list of discovered candidates.
 */
enum class ConnectionEventType {
    DISCOVERED, LOST
}