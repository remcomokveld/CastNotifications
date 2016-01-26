CastNotifications
========

[ ![Download](https://api.bintray.com/packages/rmokveld/maven/cast-notification/images/download.svg) ](https://bintray.com/rmokveld/maven/cast-notification/_latestVersion)
[![license](https://img.shields.io/hexpm/l/plug.svg)](LICENSE)

[![Build Status](https://travis-ci.org/remcomokveld/CastNotifications.svg?branch=master)](https://travis-ci.org/remcomokveld/CastNotifications)


Library for showing system notifications with cast support
* Easy starting of cast from a notification through notification actions
* Works with CastCompanionLibrary-android (but not dependent on it)

The library will activate and deactivate cast discovery based on various triggers.
For example when wifi is connected and there are active notifications a short discovery of 10s will be launched to determine if there are any ChromeCast devices on the network. When the discovery stops after 10 seconds the actions will still be available in the notification for as long as the wifi does not disconnect.

Note: since guest mode would trigger a different user interface it is currently not supported.

Usage
=====
## Setup

The easiest way to setup is if you are using CastCompanionLibrary you can also use this library without CCL but then you need to provide a custom implementation for each of the methods in th `CastCompanionLibraryInterface`

``` java
public class SampleApplication exstends Application {

    @Override public void onCreate() {

        ... initialize CastCompanionLibrary ...

        CastNotificationManager.init(this, new CastCompanionInterface() {
            // This called after the Receiver app is connected it starts the playback on ChromeCast
            public void loadMedia(MediaInfo media) {
                try {
                    VideoCastManager.getInstance().loadMedia(media, true, 0);
                } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
                    e.printStackTrace();
                }
            }

            // Provide the MediaRouteSelector which should be used for discovery
            @Override
            public MediaRouteSelector getMediaRouteSelector() {
                return VideoCastManager.getInstance().getMediaRouteSelector();
            }

            // In a custom implementation this should return true if the Receiver app is connected through a GoogleApiService
            @Override
            public boolean isApplicationConnected() {
                return VideoCastManager.getInstance().isConnected();
            }

            // CastCompanionLibrary starts the Reveiver app from onDeviceSelected.
            // Custom implementation should start the application from this method
            public void onDeviceSelected(CastDevice device) {
                VideoCastManager.getInstance().onDeviceSelected(device);
            }
        });

        // Let the library know that the receiver app is connected
        // Custom implementation should also call CastNotificationManager.getInstance().onApplicationConnected(); when the application is connected
        VideoCastManager.getInstance().addVideoCastConsumer(new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                CastNotificationManager.getInstance().onApplicationConnected();
            }
        });
    }
}
```

## Posting a notification

```java
CastNotificationManager.getInstance().notify(2, "NotificationTitle", "NotificationSubTitle", mediaInfo);
```

## Cancel a notification

```java
CastNotificationManager.getInstance().cancel(2);

```

Customization
=====

### MediaInfoSerializer
In order to update the notifications when cast availability changes the notifications are persisted by the library.
Since MediaInfo by default does not have a serialization option a custom MediaInfoSerializer interface can be set on the CastNotificationManager
to handle MediaInfoSerialization. For default serialization see the DefaultMediaInfoSerializer.

### NotificationBuildCallback
To customize how the notifications look you can also set a custom NotificationBuilder. See DefaultNotificationBuildCallback for an implementation example.

Download
--------

### Project dependencies:
The library is hosted on jcenter. To avoid version conflicts in Play Services and Support libraries, and prevent stuff from breaking when there are new versions of either of them there are seperate flavors for different dependencies.
```groovy
// For Play Services 7.5.0 Support lib 23.1.1
compile 'nl.rmokveld:cast-notification-gms75-support2311"0.1.13+'

// For Play Services 8.3.0 Support lib 23.1.1
compile 'nl.rmokveld:cast-notification-gms83-support2311"0.1.13+'

// For Play Services 8.4.0 Support lib 23.1.1
compile 'nl.rmokveld:cast-notification-gms84-support2311"0.1.13+'

```


License
-------

    Copyright 2015 Remco Mokveld

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

- WC: Wifi Connected
- SO: Screen On
- AN: Active Notification
- DT: Discovery With Timeout
- DN: Discovery No Timeout
- SD: Stop Discovery
- RT: Remove Timeout
- X: Do nothing

| Event | State               | Result |
|-------|---------------------|--------|
| AN    | SO && WC            | DT     |
| AN    | SO && !WC           | X      |
| AN    | !SO && WC           | DN     |
| AN    | !SO && !WC          | X      |
|       |                     |        |
| !AN   | SO && WC            | SD     |
| !AN   | SO && !WC           | SD     |
| !AN   | !SO && WC           | SD     |
| !AN   | !SO && !WC          | SD     |
|       |                     |        |
| SO    | AN && WC            | DT     |
| SO    | AN && !WC           | X      |
| SO    | !AN && WC           | X      |
| SO    | !AN && !WC          | X      |
|       |                     |        |
| !SO   | AN && WC            | RT     |
| !SO   | AN && !WC           | X      |
| !SO   | !AN && WC           | X      |
| !SO   | !AN && !WC          | X      |
|       |                     |        |
| WC    | AN && SO            | DT     |
| WC    | AN && !SO           | DN     |
| WC    | !AN && SO           | X      |
| WC    | !AN && !SO          | X      |
|       |                     |        |
| !WC   | AN && SO            | SD     |
| !WC   | AN && !SO           | SD     |
| !WC   | !AN && SO           | X      |
| !WC   | !AN && !SO          | X      |
|       |                     |        |
| TO    | AN && WC && SO      | SA     |
| TO    | AN && WC && !SO     | SA     |
| TO    | AN && !WC && SO     | X      |
| TO    | AN && !WC && !SO    | X      |
| TO    | !AN && WC && SO  SA | X      |
| TO    | !AN && WC && !SO    | X      |
| TO    | !AN && !WC && SO    | X      |
| TO    | !AN && !WC && !SO   | X      |
