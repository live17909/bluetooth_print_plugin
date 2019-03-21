import Flutter
import UIKit

public class SwiftBluetoothPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "com.kezaihui.bluetooth/bluetooth_method_channel", binaryMessenger: registrar.messenger())
    let instance = SwiftBluetoothPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
        case "isEnabled":

        case "isConnectDevice":
        case "startRegisterScan":
        case "stopRegisterScan":
        case "connectDeviceByAddress":
        case "connectPrinterService":
        case "disconnectPrinterService":
        case "printText":
        default:
            result(FlutterMethodNotImplemented)
        }
  }
}
