import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'rtmp_broadcaster_platform_interface.dart';

/// An implementation of [RtmpBroadcasterPlatform] that uses method channels.
class MethodChannelRtmpBroadcaster extends RtmpBroadcasterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('rtmp_broadcaster');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
