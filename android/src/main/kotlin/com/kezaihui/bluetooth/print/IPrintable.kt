package com.kezaihui.bluetooth.print

import java.io.OutputStream

/**
 *  author : fangmingxing
 *  time   : 2018/8/2
 *  desc   : 打印接口
 */
interface IPrintable {

    /**
     * 普通手机打印【蓝牙】
     *
     * @param outputStream OutputStream   打印流
     */
    fun phonePrint(outputStream: OutputStream)
}