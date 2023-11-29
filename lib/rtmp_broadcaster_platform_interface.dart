import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'rtmp_broadcaster_method_channel.dart';

abstract class RtmpBroadcasterPlatform extends PlatformInterface {
  /// Constructs a RtmpBroadcasterPlatform.
  RtmpBroadcasterPlatform() : super(token: _token);

  static final Object _token = Object();

  static RtmpBroadcasterPlatform _instance = MethodChannelRtmpBroadcaster();

  /// The default instance of [RtmpBroadcasterPlatform] to use.
  ///
  /// Defaults to [MethodChannelRtmpBroadcaster].
  static RtmpBroadcasterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [RtmpBroadcasterPlatform] when
  /// they register themselves.
  static set instance(RtmpBroadcasterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
