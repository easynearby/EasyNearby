<img src="./assets/logo.png" alt="image" width="300" height="300">

## What is EasyNearby?
EasyNearby is a wrapper around Google's [Nearby Connections API](https://developers.google.com/nearby/connections/overview)(previously known as Nearby Messages API). 

## Demo (Youtube)
<iframe width="560" height="315" src="https://www.youtube.com/embed/hAqrdB-kCzE?si=QoP-uOJ2wbY7aQJ3" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>


## API docs
Api documentation can be found [here](https://easynearby.github.io/)

## Quick Start
[Setup](#setup)<br>
[Permissions](#permissions)<br>
[Start advertising](#start-advertising)<br>
[Start discovery](#start-discovering)<br>
[Connect](#connect)<br>
[Send payload](#send-payload)<br>
[Get payload](#get-payload)<br>
[Close connection](#close-connection)<br>
[Stop advertising](#stop-advertising)<br>
[Stop discovering](#stop-discoverring)<br>


### Setup
include dependency into your project

`implementation("io.github.easynearby:android:0.0.2")`

### Permissions
The following permissions required depending on the Android API version:

```kotlin
/**
 * These permissions are required before connecting to Nearby Connections.
 */
private var REQUIRED_PERMISSIONS: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
```

### Start advertising
In order to advertise the device you have to acquire Advertiser via `EasyNearby.getAdvertiseManager()` and invoke `startAdvertising` method with a `DeviceInfo` object that represents your intention.

```kotlin
EasyNearby.getAdvertiseManager().startAdvertising(DeviceInfo("myDeviceName", "serviceId", ConnectionStrategy.STAR))
```

This will return a `Result` which in case of success gives you a flow of `ConnectionCandidateEvent`s. The event has inside `ConnectionCandidate` -  that represents a remote device and one of the two event types:

`DISCOVERED` - when a new ConnectionCandidate is found and there is a possibility to connect ot it<br>
`LOST` - when previously found ConnectionCandidate is lost and there is no possibility to connect to it

```kotlin
fun start(deviceInfo: DeviceInfo) {
    viewModelScope.launch {
        EasyNearby.getAdvertiseManager().startAdvertising(deviceInfo)
            .onSuccess {
                // listen flow events
            }.onFailure {
                // Handle failure
            }
    }
}
```

### Start discovering
Discovering process are very similar to advertising. You just have to use `EasyNearby.getDiscoverManager()` and invoke `startDiscovery` method with a `DeviceInfo` object that represents your intention.

This will return a `Result` which in case of success gives you a flow of `ConnectionCandidateEvent`s. The event has inside `ConnectionCandidate` -  that represents a remote device and one of the two event types:

`DISCOVERED` - when a new ConnectionCandidate is found and there is a possibility to connect ot it<br>
`LOST` - when previously found ConnectionCandidate is lost and there is no possibility to connect to it


```kotlin
fun start(deviceInfo: DeviceInfo) {
    viewModelScope.launch {
        EasyNearby.getDiscoverManager().startDiscovery(deviceInfo)
            .onSuccess {
                // listen flow events
            }.onFailure {
                // Handle failure
            }
    }
}
```

### Connect
In order to either initiate a connection or accept incomming connection you have to invoke `connect` method on the `ConnectionCandidate`. In this method you provide your device name and optionally you can pass `authValidator` callback in order to verify authentication digits with your partner.
In case of Success you'll get a `Connection` object that represent the established connection

```kotlin
 connectionCandidate.connect(myDeviceName ?: "unknown") { authenticationDigits: String ->
    val authResult:Boolean = showDitisToUserAndVerify(authenticationDigits)
    return authResult
 }.onSuccess {
    // handle success, for example keep connection
     saveConnection(it)
 }.onFailure {
     // handle failure
 }
```

### Send payload
In order to send payload you can use `sendPayload` function on a `Connection` that you've established

```kotlin
val message = "Hello"
connection.sendPayload(message.toByteArray())
```

### Get payload
In order to get payload you can use `getPayload` function on a `Connection` that returnes a Flow<ByteArray>. **This flow will be cancelled once the connection is closed**.

```kotlin
connection.getPayload().onCompletion {
               // Chandle connection close
           }.collect { payload ->
                // handle received payload            
           }
       }
```

### Close connection
In order to close a connection you can use `close` function on a `Connection`.

```kotlin
connection.close()
```

### Stop advertising
In order to stop advertising you can invoke `startAdvertising` method on the `AdvertiseManager`.

***Even after that devices can initiate connection if they have alredy discovered the device***

```kotlin
EasyNearby.getAdvertiseManager().stopAdvertising()
```

### Stop discoverring
In order to stop discovering you can invoke `stopDiscovery` method on the `DiscoveryManager`.

***Even after that the device can initiate connection if they have alredy discovered devices***

```kotlin
EasyNearby.getDiscoverManager().stopDiscovery()
```