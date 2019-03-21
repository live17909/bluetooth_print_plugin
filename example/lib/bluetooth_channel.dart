import 'dart:async';

import 'package:bluetooth_example/bluetooth_handler.dart';
import 'package:flutter/services.dart';

class BluetoothChannel {
  static const String METHOD_CHANNEL_NAME =
      'com.kezaihui.bluetooth/bluetooth_method_channel';

  static MethodChannel _channel = MethodChannel(METHOD_CHANNEL_NAME)
    ..setMethodCallHandler(BluetoothHandler.handler);

  static void connectPrinterService() {
    _channel.invokeMethod('connectPrinterService');
  }

  static void printText(List<Map<String, String>> maps) {
    _channel.invokeMethod('printText', maps);
  }

  static void research() {
    _channel.invokeMethod('stopRegisterScan');
    _channel.invokeMethod('startRegisterScan');
  }

  static void connectDeviceByAddress() {
    _channel.invokeMethod('connectDeviceByAddress', 'DC:0D:30:21:A1:37');
  }
}
