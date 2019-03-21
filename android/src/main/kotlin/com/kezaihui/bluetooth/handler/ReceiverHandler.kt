package com.kezaihui.bluetooth.handler


import android.bluetooth.BluetoothDevice
import com.kezaihui.bluetooth.print.PrintErrorInfo
import io.flutter.plugin.common.MethodChannel

object ReceiverHandler {

    var methodChannel: MethodChannel? = null

    fun onBltTurnOn() {
        methodChannel?.invokeMethod("onBltTurnOn", null)
    }

    fun onBltTurnOff() {
        methodChannel?.invokeMethod("onBltTurnOff", null)
    }

    fun onBltStateOn() {
        methodChannel?.invokeMethod("onBltStateOn", null)
    }

    fun onBltBonding(device: BluetoothDevice) {
        methodChannel?.invokeMethod("onBltBonding", convertDeviceToMap(device))
    }

    fun onBltBondEnd(device: BluetoothDevice) {
        methodChannel?.invokeMethod("onBltBondEnd", convertDeviceToMap(device))
    }

    fun onBltBondNone(device: BluetoothDevice) {
        methodChannel?.invokeMethod("onBltBondNone", convertDeviceToMap(device))
    }

    fun onBltFindDevice(device: BluetoothDevice) {
        methodChannel?.invokeMethod("onBltFindDevice", convertDeviceToMap(device))
    }

    fun onBltFindFinish() {
        methodChannel?.invokeMethod("onBltFindFinish", null)
    }

    fun onBltDeviceCon(device: BluetoothDevice?) {
        methodChannel?.invokeMethod("onBltDeviceCon", convertDeviceToMap(device))
    }

    fun onBltDeviceDisCon(device: BluetoothDevice?) {
        methodChannel?.invokeMethod("onBltDeviceDisCon", convertDeviceToMap(device))
    }

    val onPrintSuccess = {
        methodChannel?.invokeMethod("onPrintSuccess", null)
        Unit
    }

    val onPrintFailure = { printErrorInfo: PrintErrorInfo ->
        val map = HashMap<String, String>()
        map["title"] = printErrorInfo.title
        map["description"] = printErrorInfo.description
        methodChannel?.invokeMethod("onPrintFailure", map)
        Unit
    }

    private fun convertDeviceToMap(device: BluetoothDevice?): Map<String, Any>? {
        return if (device != null) {
            val map = HashMap<String, Any>()
            map["type"] = device.type
            map["name"] = device.name
            map["address"] = device.address
            map["bondState"] = device.bondState
            map
        } else {
            null
        }
    }
}