package com.kezaihui.bluetooth.print

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Message
import android.util.Log
import com.kezaihui.bluetooth.listener.BtStateChangeListener
import com.kezaihui.bluetooth.receiver.BluetoothDevicesReceiver
import java.util.*

/**
 *  author : fangmingxing
 *  time   : 2018/03/27
 *  desc   : 打印管理工具类
 */
object UtilBluetooth {

    const val REQUEST_BLUETOOTH_ON = 100                      // 请求打开蓝牙ID
    private const val BLUETOOTH_DISCOVERABLE_DURATION = 300   // 蓝牙可见时长

    var bluetoothDeviceSocket: BluetoothSocket? = null        // 蓝牙通信socket
    private val UUID_DEVICES = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")    // 通信uuid
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()    // android系统蓝牙管理
    private var mBluetoothDevices: MutableSet<BluetoothDevice> = mutableSetOf()    // 所有搜索到的蓝牙设备
    private var mStateChangeListener: IBtStateChangeListener? = null // 蓝牙状态监听回调
    private var mBluetoothDevicesReceiver: BluetoothDevicesReceiver? = null
    private const val MSG_CONNECT_DEVICE_SUCCESS = 1000
    private const val MSG_DISCONNECT_DEVICE_SUCCESS = 1001

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_CONNECT_DEVICE_SUCCESS -> getStateChangeListener()?.onBltDeviceCon(msg.obj as BluetoothDevice)
                MSG_DISCONNECT_DEVICE_SUCCESS -> getStateChangeListener()?.onBltDeviceDisCon(msg.obj as BluetoothDevice)
                else -> {
                }
            }
        }
    }


    /**
     * 开始初始化蓝牙打印
     *
     * @param context Context                              context上下文
     * @param stateChangeListener BtStateChangeListener?   蓝牙状态的监听回调
     */
    fun startBluetoothPrint(context: Context, stateChangeListener: IBtStateChangeListener? = BtStateChangeListener()) {
        if (mBluetoothAdapter == null) return
        if (stateChangeListener != null) mStateChangeListener = stateChangeListener
        mBluetoothDevicesReceiver = BluetoothDevicesReceiver()
        mBluetoothDevicesReceiver?.registerBy(context)
        startRegisterScan(context)
    }

    /**
     * 停止蓝牙打印
     *
     * @param isResetBluetoothDevice Boolean     是否重新配置蓝牙设备，false: 不断开打印机 true: 断开打印机
     * @param handleDisConnectEvent () -> Unit   断开时自定义处理事件
     */
    fun stopBluetoothPrint(context: Context, isResetBluetoothDevice: Boolean = true, handleDisConnectEvent: () -> Unit = {}) {
        if (isResetBluetoothDevice) disConnectDevice(bluetoothDeviceSocket?.remoteDevice, handleDisConnectEvent)
        mStateChangeListener = null
        stopRegisterScan()
    }

    /**
     * 连接蓝牙
     *
     * @param device BluetoothDevice   目标连接设备
     */
    fun connectBond(device: BluetoothDevice) {
        if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.IMAGING) {
            if (device.bondState == BluetoothDevice.BOND_BONDED && mBluetoothAdapter.bondedDevices.contains(device)) {
                connectDevice(device)
            } else if (mStateChangeListener != null) device.createBond()
        }
    }

    /**
     * 添加蓝牙设备
     *
     * @param device BluetoothDevice   目标连接设备
     */
    fun addBluetoothDevice(device: BluetoothDevice) {
        mBluetoothDevices.add(device)
        if (!isConnectDevice()) connectBond(device) // 搜索到蓝牙打印机设备后，自动连接设备
    }

    /**
     * 是否连接了打印机
     *
     * @return Boolean   是否连接了打印设备
     */
    fun isConnectDevice() = isEnabled()
            && bluetoothDeviceSocket?.remoteDevice?.bondState == BluetoothDevice.BOND_BONDED
            && bluetoothDeviceSocket?.isConnected == true

    /**
     * 获取状态更改监听器
     *
     * @return BtStateChangeListener?   状态监听器
     */
    fun getStateChangeListener() = mStateChangeListener

    fun connectDeviceByAddress(address: String) {
        mBluetoothDevices.forEach {
            if (it.address == address) {
                connectBond(it)
                return@forEach
            }
        }
    }

    /**
     * 连接打印机设备通信
     *
     * @param device BluetoothDevice   目标通信打印机
     */
    fun connectDevice(device: BluetoothDevice) {
        Log.d("connectDevice", "connectDevice")
        if (bluetoothDeviceSocket != null
                && bluetoothDeviceSocket?.isConnected == true
                && device == bluetoothDeviceSocket?.remoteDevice
        ) return
        Thread(Runnable
        {
            try {
                bluetoothDeviceSocket = device.createRfcommSocketToServiceRecord(UUID_DEVICES)
                bluetoothDeviceSocket?.connect()
                mHandler.obtainMessage(MSG_CONNECT_DEVICE_SUCCESS, device)
                Log.d("connectDevice", device.name)
            } catch (e: Exception) {
                bluetoothDeviceSocket?.close()
                bluetoothDeviceSocket = null
            }
        }).start()
    }

    /**
     * 断开打印机设备通信
     *
     * @param device BluetoothDevice?            目标通信打印机
     * @param handleDisConnectEvent () -> Unit   断开时自定义处理事件
     */
    fun disConnectDevice(device: BluetoothDevice?, handleDisConnectEvent: () -> Unit) {
        if (bluetoothDeviceSocket != null
                && bluetoothDeviceSocket?.isConnected == true
                && device == bluetoothDeviceSocket?.remoteDevice
        ) {
            handleDisConnectEvent()
            Thread(Runnable
            {
                try {
                    bluetoothDeviceSocket?.close()
                    bluetoothDeviceSocket = null
                    mHandler.obtainMessage(MSG_DISCONNECT_DEVICE_SUCCESS, device)
                } catch (e: Exception) {
                    bluetoothDeviceSocket = null
                }
            }).start()
        }
    }

    /**
     * 停止扫描周边蓝牙设备
     */
    fun stopRegisterScan() {
        if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
    }

    /**
     * 是否开启蓝牙
     *
     * @return Boolean   是否开启蓝牙设备
     */
    fun isEnabled() = mBluetoothAdapter.isEnabled

    /**
     * 开始扫描周边蓝牙设备
     *
     * @param context Context   context上下文
     */
    fun startRegisterScan(context: Context) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) return
        else {
            if (!isEnabled())
                if (mStateChangeListener == null) mBluetoothAdapter.enable() else {
                    if (context !is Activity) return
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    intent.action = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION)
                    context.startActivityForResult(intent, REQUEST_BLUETOOTH_ON)
                }
            else {
                stopRegisterScan()
                mBluetoothAdapter.startDiscovery()
            }
        }
    }
}