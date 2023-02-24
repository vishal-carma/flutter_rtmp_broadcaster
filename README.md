# Flutter RTMP broadcaster

RTMP streaming and camera plugin.

## Getting Started

This plugin is an extension of the Flutter
[camera plugin](https://pub.dev/packages/camera) to add in
rtmp streaming as part of the system. It works on android and iOS
(but not web).

This means the API Is exactly the same as the camera and
installation requirements are the same. The different is there
is an extra API that is startStreaming(url) that takes an rtmp
url and starts streaming to that specific url.

For android I
use [rtmp-rtsp-stream-client-java](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java)
and for iOS I use
[HaishinKit.swift](https://github.com/shogo4405/HaishinKit.swift)

## Features:

* Display live camera preview in a widget.
* Snapshots can be captured and saved to a file.
* Record video.
* Add access to the image stream from Dart.

## Installation

First, add `camera` as a [dependency in your pubspec.yaml file](https://flutter.io/using-packages/).

### iOS

Add two rows to the `ios/Runner/Info.plist`:

* one with the key `Privacy - Camera Usage Description` and a usage description.
* and one with the key `Privacy - Microphone Usage Description` and a usage description.

Or in text format add the key:

```
    <key>NSAppTransportSecurity</key>
    <dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
    </dict>
    <key>NSPhotoLibraryUsageDescription</key>
    <string></string>
    <key>UIBackgroundModes</key>
    <array>
    <string>processing</string>
    </array>
    <key>NSCameraUsageDescription</key>
    <string>App requires access to the camera for live streaming feature.</string>
    <key>NSMicrophoneUsageDescription</key>
    <string>App requires access to the microphone for live streaming feature.</string>
```

## Example

To see an example implementation of this plugin, please refer to
the [example code](https://github.com/emiliodallatorre/flutter_rtmp_publisher/tree/master/example).
This includes a demonstration of how to use the `rtmp_publisher` plugin to stream live video to MUX,
as well as how to capture snapshots and record video. Simply clone the repository and run the app on
an Android or iOS device.

### Android

Change the minimum Android sdk version to 21 (or higher) in your `android/app/build.gradle` file.

```
    minSdkVersion 21
```

and add the following part to your `android/app/src/build.gradle` file:

```
android {
    ...
    packagingOptions {
        exclude 'project.clj'
    }
}
```

in order to prevent packaging problems.

# Note for MUX Server

* RTMP Url (for publisher): rtmp://live.mux.com/app/${streamkey}
* Stream Url (for client): https://stream.mux.com/${playbackId}.m3u8
* Poster Url (for preview): https://image.mux.com/${playbackId}/thumbnail.jpg
