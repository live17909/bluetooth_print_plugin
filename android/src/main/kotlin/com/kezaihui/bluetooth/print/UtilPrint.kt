package com.kezaihui.bluetooth.print

import android.content.Context
import android.text.TextUtils
import java.io.UnsupportedEncodingException

/**
 *  author : fangmingxing
 *  time   : 2017/12/27
 *  desc   : 打印管理工具类
 */
object UtilPrint {
    private val TAG = this::class.java.simpleName

    const val ALIGN_LEFT = 0                          // 填充时，原内容居左
    const val ALIGN_RIGHT = 1                         // 填充时，原内容居右
    const val ALIGN_MIDDLE = 2                        // 填充时，原内容居中

    private lateinit var mPrintClient: IPrintClient   // 打印代理

    /**
     * 填充打印字符，补满一行
     *
     * @param input String    要打印的内容
     * @param num Int         最小长度，不足的会用空格补充
     * @param alignment Int   0：内容居左；1：内容居右；2：内容居中
     * @param space String    待填充字符
     * @return String         补满后完整的字符串
     */
    fun addPrintSpace(input: String, num: Int, alignment: Int = ALIGN_LEFT, space: String = " "): String {
        val sb = StringBuffer()
        if (TextUtils.isEmpty(input)) {
            for (i in 0 until num) sb.append(space)
        } else {
            //计算字符长度
            var inputLength = 0
            try {
                inputLength = input.toByteArray(charset("gbk")).size//中文：2个字符；英文：1个字符
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            if (inputLength >= num) return input

            sb.append(input)
            val spaceLength = num - inputLength

            when (alignment) {
                ALIGN_RIGHT -> {
                    for (i in 0 until spaceLength) sb.insert(0, space)
                }
                ALIGN_MIDDLE -> {
                    val leftLength = spaceLength / 2
                    for (i in 0 until leftLength) sb.insert(0, space)
                    for (i in leftLength until spaceLength) sb.append(space)
                }
                ALIGN_LEFT -> {
                    for (i in 0 until spaceLength) sb.append(space)
                }
            }
        }

        return sb.toString()
    }

    /**
     * 把一个字符串分组，每组字符串转换为gbk的byte[]长度最接近maxByteLength
     *
     * @param source String        要分组的字符串内容
     * @param maxByteLength Int    每组字符串以gbk字符集测量时需要接近的长度
     * @return ArrayList<String>   分组后的字符串集合
     */
    fun splitByByteLength(source: String, maxByteLength: Int): ArrayList<String> {
        val texts = ArrayList<String>()
        val gbkByteLength = getByteLength(source, "gbk")
        if (gbkByteLength <= maxByteLength) {
            texts.add(source)
            return texts
        }
        var subStringStart = 0
        var subStringEnd = 0// max -> source.length
        while (true) {
            if (subStringEnd >= source.length) {
                texts.add(source.substring(subStringStart, source.length))
                return texts
            } else if (subStringStart != subStringEnd) {
                texts.add(source.substring(subStringStart, subStringEnd))
                subStringStart = subStringEnd
            }
            var start = maxByteLength / 2
            var end = maxByteLength

            while (true) {
                if (end < start) {
                    subStringEnd = subStringStart + end
                    break
                }
                val mid = (start + end) / 2
                subStringEnd = subStringStart + mid
                //                System.out.println("while 2 ---- ---- substring " + subStringStart + "||" + subStringEnd);

                subStringEnd = if (subStringEnd >= source.length) source.length else subStringEnd
                val d = getByteLength(source.substring(subStringStart, subStringEnd), "gbk")
                val gap = d - maxByteLength
                if (gap > 0) {
                    end = mid - 1
                } else if (gap == 0) {
                    subStringEnd = subStringStart + mid
                    break
                } else {
                    start = mid + 1
                }
            }
        }
    }

    /**
     * 获取打印委托
     *
     * @return IPrintClient   打印委托
     */
    fun getPrintClient() = mPrintClient

    /**
     * 连接打印服务
     *
     * @param context Context     context上下文
     * @param deviceType String   设备类型
     * @param fontSize FontSize   相对字符大小
     */
    fun connectPrinterService(context: Context, deviceType: String = "", fontSize: PrintFormat.Companion.AbsoluteFontSize = PrintFormat.Companion.AbsoluteFontSize.SIZE0) {
        PrintFormat.baseAbsFontSize = fontSize
        mPrintClient = PhonePrintClient(true)
        mPrintClient.connectPrinterService(context)
    }

    /**
     * 断开打印服务
     *
     * @param context Context                    context上下文
     * @param handleDisConnectEvent () -> Unit   自定义处理打印断开事件
     */
    fun disconnectPrinterService(context: Context, handleDisConnectEvent: () -> Unit = {}) {
        mPrintClient.disconnectPrinterService(context, handleDisConnectEvent)
    }

    /**
     * 以gbk字符集为准，将字符串转为byte数组
     *
     * @param string String   待处理字符串
     * @return ByteArray      目标数组
     */
    fun getGbkByte(string: String): ByteArray {
        var bytes = ByteArray(0)
        try {
            bytes = string.toByteArray(charset("gbk"))
        } catch (ignored: UnsupportedEncodingException) {
            ignored.printStackTrace()
        }
        return bytes
    }

    /**
     * 以gbk字符集为准，获取字符的长度
     *
     * @param input String   需要测量的字符串
     * @return Int           字符串长度
     */
    fun getGBKLength(input: String): Int = getByteLength(input, "gbk")

    /**
     * 以某个字符集为准，获取字符的长度
     *
     * @param input String   需要测量的字符串
     * @return Int           字符串长度
     */
    private fun getByteLength(input: String, charset: String): Int {
        try {
            return input.toByteArray(charset(charset)).size//中文：2个字符；英文：1个字符
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return 0
    }

    fun convertMapToPrintable(maps: List<Map<String, Any>>?): List<IPrintable> {
        return maps?.map {
            when (it["type"]) {
                "Title" -> PrintFormat.Title(it["value"].toString(), convertIntToRelativeFontSize(it["fontSize"].toString(), 2))
                "LineText" -> PrintFormat.LineText(it["value"].toString(), it["space"]?.toString()
                        ?: "*", convertIntToRelativeFontSize(it["fontSize"].toString(), 1))
                "Left" -> PrintFormat.Left(it["value"].toString(), convertIntToRelativeFontSize(it["fontSize"].toString(), 1))
                "Right" -> PrintFormat.Right(it["value"].toString(), convertIntToRelativeFontSize(it["fontSize"].toString(), 1))
                "FontsRight" -> PrintFormat.FontsRight(it["values"] as Array<String>,
                        ((it["fontSizes"] as IntArray).map { item -> convertIntToRelativeFontSize(item.toString(), 1) }).toTypedArray())
                "LeftRight" -> PrintFormat.LeftRight(it["left"].toString(), it["right"].toString(),
                        convertIntToRelativeFontSize(it["fontSize"].toString(), 1))
                "MultiPart" -> PrintFormat.MultiPart(it["values"] as Array<String>, it["lengths"] as IntArray,
                        convertIntToRelativeFontSize(it["fontSize"].toString(), 1))
                "Line" -> PrintFormat.Line(convertIntToRelativeFontSize(it["fontSize"].toString(), 1))
                "BreakLine" -> PrintFormat.BreakLine(it["count"]?.toString()?.toInt()
                        ?: 3, it["space"]?.toString()
                        ?: "")
                else -> PrintFormat.BreakLine(it["count"]?.toString()?.toInt()
                        ?: 1, it["space"]?.toString()
                        ?: "")
            }
        } ?: listOf()
    }

    private fun convertIntToRelativeFontSize(fontSize: String?, default: Int): PrintFormat.Companion.RelativeFontSize {
        val realFontSize = fontSize ?: default.toString()
        return when (realFontSize) {
            "0" -> PrintFormat.Companion.RelativeFontSize.Relative0
            "1" -> PrintFormat.Companion.RelativeFontSize.Relative1
            "2" -> PrintFormat.Companion.RelativeFontSize.Relative2
            "3" -> PrintFormat.Companion.RelativeFontSize.Relative3
            else -> PrintFormat.Companion.RelativeFontSize.Relative1
        }
    }
}