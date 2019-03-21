import 'package:flutter/services.dart';

class BluetoothHandler {
  static Future<dynamic> handler(MethodCall methodCall) {
    switch (methodCall.method) {
      case 'onBltFindDevice':
        print('搜索蓝牙设备' + methodCall.arguments['name'] + '：' + methodCall.arguments['address']);
        break;
      case 'onBltTurnOn':
        print('onBltTurnOn');
        break;
      case 'onBltTurnOff':
        print('onBltTurnOff');
        break;
      case 'onBltStateOn':
        print('onBltStateOn');
        break;
      case 'onBltDeviceCon':
        print('连接蓝牙设备' + methodCall.arguments['name']);
        break;
      default:
    }

    return Future.value(true);
  }
}
