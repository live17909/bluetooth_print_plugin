package com.kezaihui.bluetooth.listener

import android.bluetooth.BluetoothDevice
import com.kezaihui.bluetooth.handler.ReceiverHandler
import com.kezaihui.bluetooth.print.IBtStateChangeListener

class BtStateChangeListener : IBtStateChangeListener {

    override fun onBltTurnOn() {
        ReceiverHandler.onBltTurnOn()
    }

    override fun onBltTurnOff() {
        ReceiverHandler.onBltTurnOff()
    }

    override fun onBltStateOn() {
        ReceiverHandler.onBltStateOn()
    }

    override fun onBltBonding(device: BluetoothDevice) {
        ReceiverHandler.onBltBonding(device)
    }

    override fun onBltBondEnd(device: BluetoothDevice) {
        ReceiverHandler.onBltBondEnd(device)
    }

    override fun onBltBondNone(device: BluetoothDevice) {
        ReceiverHandler.onBltBondNone(device)
    }

    override fun onBltFindDevice(device: BluetoothDevice) {
        if (device.)
        ReceiverHandler.onBltFindDevice(device)
    }

    override fun onBltFindFinish() {
        ReceiverHandler.onBltFindFinish()
    }

    override fun onBltDeviceCon(device: BluetoothDevice?) {
        ReceiverHandler.onBltDeviceCon(device)
    }

    override fun onBltDeviceDisCon(device: BluetoothDevice?) {
        ReceiverHandler.onBltDeviceDisCon(device)
    }
}