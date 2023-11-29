import 'package:flutter_test/flutter_test.dart';
import 'package:rtmp_broadcaster/rtmp_broadcaster.dart';
import 'package:rtmp_broadcaster/rtmp_broadcaster_platform_interface.dart';
import 'package:rtmp_broadcaster/rtmp_broadcaster_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockRtmpBroadcasterPlatform
    with MockPlatformInterfaceMixin
    implements RtmpBroadcasterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final RtmpBroadcasterPlatform initialPlatform = RtmpBroadcasterPlatform.instance;

  test('$MethodChannelRtmpBroadcaster is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelRtmpBroadcaster>());
  });

  test('getPlatformVersion', () async {
    RtmpBroadcaster rtmpBroadcasterPlugin = RtmpBroadcaster();
    MockRtmpBroadcasterPlatform fakePlatform = MockRtmpBroadcasterPlatform();
    RtmpBroadcasterPlatform.instance = fakePlatform;

    expect(await rtmpBroadcasterPlugin.getPlatformVersion(), '42');
  });
}
