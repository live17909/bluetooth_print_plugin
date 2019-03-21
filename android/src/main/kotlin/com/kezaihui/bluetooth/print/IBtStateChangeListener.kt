package com.kezaihui.bluetooth.print

import android.bluetooth.BluetoothDevice

/**
 *  author : fangmingxing
 *  time   : 2018/4/12
 *  desc   : 蓝牙状态监听
 */
interface IBtStateChangeListener {

    /**
     * 打开蓝牙
     */
    fun onBltTurnOn()

    /**
     * 关闭蓝牙
     */
    fun onBltTurnOff()

    /**
     * 蓝牙已准备好
     */
    fun onBltStateOn()

    /**
     * 蓝牙正在绑定中
     *
     * @param device BluetoothDevice   目标蓝牙设备
     */
    fun onBltBonding(device: BluetoothDevice)

    /**
     * 蓝牙绑定结束
     *
     * @param device BluetoothDevice   目标蓝牙设备
     */
    fun onBltBondEnd(device: BluetoothDevice)

    /**
     * 蓝牙绑定失败
     *
     * @param device BluetoothDevice   目标蓝牙设备
     */
    fun onBltBondNone(device: BluetoothDevice)

    /**
     * 搜寻发现蓝牙设备
     *
     * @param device BluetoothDevice   目标蓝牙设备
     */
    fun onBltFindDevice(device: BluetoothDevice)

    /**
     * 搜索设备结束
     */
    fun onBltFindFinish()

    /**
     * 连接蓝牙设备
     *
     * @param device BluetoothDevice?   目标蓝牙设备
     */
    fun onBltDeviceCon(device: BluetoothDevice?)

    /**
     * 断开蓝牙设备
     *
     * @param device BluetoothDevice?   目标蓝牙设备
     */
    fun onBltDeviceDisCon(device: BluetoothDevice?)
}