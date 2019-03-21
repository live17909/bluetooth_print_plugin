package com.kezaihui.bluetooth.print

import android.content.Context
import android.graphics.Bitmap

/**
 *  author : fangmingxing
 *  time   : 2018/8/2
 *  desc   : 提供支持的打印方式
 */
interface IPrintClient {

    /**
     * 自定义打印 支持文本和图片（Bitmap）混合打印
     *
     * @param printList List<IPrintable>        待打印内容
     * @param onPrintSuc () -> Unit                  打印成功的回调
     * @param onPrintErr (PrintErrorInfo) -> Unit    打印失败的回调
     */
    fun printTextAndImage(
        printList: List<IPrintable>,
        onPrintSuc: () -> Unit,
        onPrintErr: (PrintErrorInfo) -> Unit
    )

    /**
     * 打印自定义文本
     *
     * @param printList List<IPrintable>?       打印内容
     * @param onPrintSuc () -> Unit                  打印成功的回调
     * @param onPrintErr (PrintErrorInfo) -> Unit    打印失败的回调
     */
    fun printText(
        printList: List<IPrintable>?,
        onPrintSuc: () -> Unit = {},
        onPrintErr: (PrintErrorInfo) -> Unit = {}
    )

    /**
     * 打印位图
     *
     * @param bitmap Bitmap                          待打印位图，注意白色是打不出来的，透明打出来是黑色
     * @param left Int                               为了适配lkl设备增加的字段，summi可设置打印对齐方式
     * @param onPrintSuc () -> Unit                  打印成功的回调
     * @param onPrintErr (PrintErrorInfo) -> Unit    打印失败的回调
     */
    fun printBitmap(bitmap: Bitmap, left: Int, onPrintSuc: () -> Unit = {}, onPrintErr: (PrintErrorInfo) -> Unit = {})

    /**
     * 打印二维码
     *
     * @param qrCode String                           待打印二维码内容
     * @param onPrintSuc () -> Unit                   打印成功的回调
     * @param onPrintError (PrintErrorInfo) -> Unit   打印失败的回调
     */
    fun printQrCode(qrCode: String, onPrintSuc: () -> Unit, onPrintError: (PrintErrorInfo) -> Unit)

    /**
     * 连接打印服务
     */
    fun connectPrinterService(context: Context)

    /**
     * 断开打印服务
     *
     * @param handleDisConnectEvent () -> Unit        自定义处理断开事件
     */
    fun disconnectPrinterService(context: Context, handleDisConnectEvent: () -> Unit = {})
}