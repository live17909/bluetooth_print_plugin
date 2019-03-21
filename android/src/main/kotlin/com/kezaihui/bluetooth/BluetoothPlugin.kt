package com.kezaihui.bluetooth

import com.kezaihui.bluetooth.handler.ReceiverHandler
import com.kezaihui.bluetooth.print.UtilBluetooth
import com.kezaihui.bluetooth.print.UtilPrint
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class BluetoothPlugin(private val registrar: Registrar) : MethodCallHandler {
    companion object {
        private const val METHOD_CHANNEL_NAME = "com.kezaihui.bluetooth/bluetooth_method_channel"

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), METHOD_CHANNEL_NAME)
            channel.setMethodCallHandler(BluetoothPlugin(registrar))
            ReceiverHandler.methodChannel = channel
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "isEnabled" -> UtilBluetooth.isEnabled()
            "isConnectDevice" -> UtilBluetooth.isConnectDevice()
            "startRegisterScan" -> UtilBluetooth.startRegisterScan(registrar.activity())
            "stopRegisterScan" -> UtilBluetooth.stopRegisterScan()
            "connectDeviceByAddress" -> UtilBluetooth.connectDeviceByAddress(call.arguments as String)
            "connectPrinterService" -> UtilPrint.connectPrinterService(registrar.activity())
            "disconnectPrinterService" -> UtilPrint.disconnectPrinterService(registrar.activity())
            "printText" -> UtilPrint.getPrintClient().printText(UtilPrint.convertMapToPrintable(call.arguments as List<Map<String, Any>>), ReceiverHandler.onPrintSuccess, ReceiverHandler.onPrintFailure)
            else -> result.notImplemented()
        }
    }
}
