import 'dart:async';

import 'package:flutter/services.dart';

class Bluetooth {
  static const String METHOD_CHANNEL_NAME =
      'com.kezaihui.bluetooth/bluetooth_method_channel';

  static const MethodChannel _channel = const MethodChannel(METHOD_CHANNEL_NAME);
}
