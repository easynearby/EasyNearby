package io.github.easynearby.core

/**
 * Nearby Connections supports different strategies for advertising and discovery. The best strategy to use depends on the use case.
 *
 */
enum class ConnectionStrategy {
    /**
     *The cluster strategy is a peer-to-peer strategy that supports an M-to-N, or cluster-shaped, connection topology. In other words, this enables connecting amorphous clusters of devices within radio range (~100m), where each device can both initiate outgoing connections to M other devices and accept incoming connections from N other devices.
     *
     * This strategy is more flexible in its topology constraints than the star strategy, but results in lower bandwidth connections. It is good for use cases with smaller payloads that require a more mesh-like experience, such as multiplayer gaming.
     */
    CLUSTER,

    /**
     *The star strategy is a peer-to-peer strategy that supports a 1-to-N, or star-shaped, connection topology. In other words, this enables connecting devices within radio range (~100m) in a star shape, where each device can, at any given time, play the role of either a hub (where it can accept incoming connections from N other devices), or a spoke (where it can initiate an outgoing connection to a single hub), but not both.
     *
     * This strategy lends itself best to situations where there is one device advertising, and N devices which discover the advertiser, though you may still advertise and discover simultaneously if required.
     *
     * This strategy is more strict in its topology constraints than the cluster strategy, but results in higher bandwidth connections. It is good for high-bandwidth use cases such as sharing a video to a group of friends.
     */
    STAR,

    /**
     * The point-to-point strategy is a peer-to-peer strategy that supports a 1-to-1 connection topology. In other words, this enables connecting devices within radio range (~100m) with the highest possible throughput, but does not allow for more than a single connection at a time.
     *
     * This strategy lends itself best to situations where transferring data is more important than the flexibility of maintaining multiple connections.
     *
     * This strategy is more strict in its topology constraints than the star strategy, but results in higher bandwidth connections. It is good for high-bandwidth use cases such as sharing a large video to another device.
     */
    POINT_TO_POINT
}