import 'package:bluetooth_example/bluetooth_channel.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bluetooth/bluetooth.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              RaisedButton(
                child: Text('开启蓝牙打印'),
                onPressed: () {
                  BluetoothChannel.connectPrinterService();
                },
              ),
              RaisedButton(
                child: Text('重新搜索蓝牙'),
                onPressed: () {
                  BluetoothChannel.research();
                },
              ),
              RaisedButton(
                child: Text('连接打印机'),
                onPressed: () {
                  BluetoothChannel.connectDeviceByAddress();
                },
              ),
              RaisedButton(
                child: Text('测试打印'),
                onPressed: () {
                  List<Map<String, String>> maps = List<Map<String, String>>();
                  maps.add({
                    'type': 'Title',
                    'value': '测试标题',
                  });
                  maps.add({
                    'type': 'Line',
                  });
                  maps.add({
                    'type': 'LeftRight',
                    'left': '左边',
                    'right': '右边',
                  });
                  BluetoothChannel.printText(maps);
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}
