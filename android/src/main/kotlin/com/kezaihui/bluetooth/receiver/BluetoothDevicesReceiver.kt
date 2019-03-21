package com.kezaihui.bluetooth.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kezaihui.bluetooth.print.UtilBluetooth

/**
 * 蓝牙广播
 *
 * Created  by fangmingxing on 28/03/2018.
 */
class BluetoothDevicesReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
            when (state) {
                BluetoothAdapter.STATE_TURNING_ON -> UtilBluetooth.getStateChangeListener()?.onBltTurnOn()
                BluetoothAdapter.STATE_ON -> {
                    UtilBluetooth.getStateChangeListener()?.onBltStateOn()
                    UtilBluetooth.startBluetoothPrint(context)
                }
            }
        }
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) UtilBluetooth.getStateChangeListener()?.onBltFindFinish()
        else {
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    ?: return
            if (BluetoothDevice.ACTION_FOUND == action) {
                if (device.name != null && !device.name.isEmpty()) {
                    UtilBluetooth.addBluetoothDevice(device)
                    UtilBluetooth.getStateChangeListener()?.onBltFindDevice(device)
                }
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                when (device.bondState) {
                    BluetoothDevice.BOND_BONDING -> UtilBluetooth.getStateChangeListener()?.onBltBonding(device)
                    BluetoothDevice.BOND_BONDED -> {
                        UtilBluetooth.connectDevice(device)
                        UtilBluetooth.getStateChangeListener()?.onBltBondEnd(device)
                    }
                    BluetoothDevice.BOND_NONE -> UtilBluetooth.getStateChangeListener()?.onBltBondNone(device)
                }
            }
        }
    }

    fun registerBy(context: Context) {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        })
    }

    fun unregisterBy(context: Context) {
        context.unregisterReceiver(this)
    }
}