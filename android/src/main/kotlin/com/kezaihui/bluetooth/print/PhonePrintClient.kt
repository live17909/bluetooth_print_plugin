package com.kezaihui.bluetooth.print

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.util.*

/**
 *  author : fangmingxing
 *  time   : 2018/3/29
 *  desc   : 普通设备打印工具
 *
 *  @see IPrintClient
 */
class PhonePrintClient(private val isSupportBluetooth: Boolean = false) : IPrintClient {

    /** @see IPrintClient */
    override fun printTextAndImage(
            printList: List<IPrintable>,
            onPrintSuc: () -> Unit,
            onPrintErr: (PrintErrorInfo) -> Unit
    ) {
        if (!isSupportBluetooth) return
        Log.d(TAG, METHOD_PRINT_TEXT_IMAGE)
        print(printList, onPrintSuc, onPrintErr)
    }

    /** @see IPrintClient */
    override fun printText(
            printList: List<IPrintable>?,
            onPrintSuc: () -> Unit,
            onPrintErr: (PrintErrorInfo) -> Unit
    ) {
        if (!isSupportBluetooth) return
        Log.d(TAG, METHOD_PRINT_TEXT)
        print(printList, onPrintSuc, onPrintErr)
    }

    /** @see IPrintClient */
    override fun printBitmap(bitmap: Bitmap, left: Int, onPrintSuc: () -> Unit, onPrintErr: (PrintErrorInfo) -> Unit) {
        if (!isSupportBluetooth) return
        Log.d(TAG, "$METHOD_PRINT_BITMAP|left:$left|width:${bitmap.width}|height:${bitmap.height}")
        onPrintSuc()
    }

    /** @see IPrintClient */
    override fun printQrCode(qrCode: String, onPrintSuc: () -> Unit, onPrintError: (PrintErrorInfo) -> Unit) {
        if (!isSupportBluetooth) return
    }

    /** @see IPrintClient */
    override fun connectPrinterService(context: Context) {
        if (!isSupportBluetooth) return
        UtilBluetooth.startBluetoothPrint(context)
    }

    /** @see IPrintClient */
    override fun disconnectPrinterService(context: Context, handleDisConnectEvent: () -> Unit) {
        if (!isSupportBluetooth) return
        UtilBluetooth.stopBluetoothPrint(context, handleDisConnectEvent = handleDisConnectEvent)
    }

    /**
     * 开始打印
     *
     * @param printList ArrayList<IPrintable>?       待打印内容
     * @param onPrintSuc () -> Unit                  打印成功回调
     * @param onPrintErr (PrintErrorInfo) -> Unit    打印失败回调
     */
    private fun print(
            printList: List<IPrintable>?,
            onPrintSuc: () -> Unit = {},
            onPrintErr: (PrintErrorInfo) -> Unit = {}
    ) {
        if (!isSupportBluetooth) return
        if (!UtilBluetooth.isConnectDevice()) {
            onPrintErr(PrintErrorInfo("打印失败", "蓝牙通信连接失败"))
            return
        }
        if (printList != null && printList.isNotEmpty()) {
            printList.forEach { it.phonePrint(UtilBluetooth.bluetoothDeviceSocket!!.outputStream) }
        }
        onPrintSuc()
    }

    companion object {
        private val TAG = this::class.java.canonicalName

        private const val METHOD_PRINT_TEXT_IMAGE = "printTextImage"
        private const val METHOD_PRINT_TEXT = "printText"
        private const val METHOD_PRINT_BITMAP = "printBitmap"
    }
}
