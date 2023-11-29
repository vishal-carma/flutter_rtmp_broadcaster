
import 'rtmp_broadcaster_platform_interface.dart';

class RtmpBroadcaster {
  Future<String?> getPlatformVersion() {
    return RtmpBroadcasterPlatform.instance.getPlatformVersion();
  }
}
