package com.kezaihui.bluetooth.print

import android.graphics.Bitmap
import java.io.OutputStream
import java.nio.charset.Charset

/**
 *  author : fangmingxing
 *  time   : 2018/8/7
 *  desc   : 打印格式
 */
class PrintFormat {

    /**
     * 打印title
     *
     * @see IPrintable
     * @property text String                 待打印title文案
     * @property fontSize RelativeFontSize   相对字体大小
     */
    class Title(val text: String, private val fontSize: RelativeFontSize = RelativeFontSize.Relative2) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            initFontSize(outputStream, fontSize)
            outputStream.write(ALIGN_CENTER)
            writeText(outputStream, text)
            breakLineFlush(outputStream)
        }
    }

    /**
     * 打印行文字
     *
     * @see IPrintable
     * @property centerText String           待打印文案
     * @property space String                一行不够时，待补文案
     * @property fontSize RelativeFontSize   相对字体大小
     */
    class LineText(
            val centerText: String,
            private val space: String = "*",
            private val fontSize: RelativeFontSize = RelativeFontSize.Relative1
    ) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            val textSize = UtilPrint.getGBKLength(centerText)
            val text = UtilPrint.addPrintSpace(centerText, DEFAULT_LENGTH, UtilPrint.ALIGN_MIDDLE, space)
            val textLine = text.substring(0, text.indexOf(centerText) - textSize / 2)
            outputStream.write(NORMAL)
            outputStream.write(BOLD_CANCEL)
            writeText(outputStream, textLine)
            initFontSize(outputStream, fontSize)
            writeText(outputStream, centerText)
            outputStream.write(NORMAL)
            outputStream.write(BOLD_CANCEL)
            writeText(outputStream, textLine)
            breakLineFlush(outputStream)
        }

    }

    /**
     * 打印靠左文字
     *
     * @see IPrintable
     * @property text String                 待打印文案
     * @property fontSize RelativeFontSize   相对字体大小
     */
    class Left(val text: String, private val fontSize: RelativeFontSize = RelativeFontSize.Relative1) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            initFontSize(outputStream, fontSize)
            outputStream.write(ALIGN_LEFT)
            writeText(outputStream, text)
            breakLineFlush(outputStream)
        }
    }

    /**
     * 打印靠右文字
     *
     * @see IPrintable
     * @property text String                 待打印文案
     * @property fontSize RelativeFontSize   相对字体大小
     */
    class Right(val text: String, private val fontSize: RelativeFontSize = RelativeFontSize.Relative1) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            initFontSize(outputStream, fontSize)
            outputStream.write(ALIGN_RIGHT)
            writeText(outputStream, text)
            breakLineFlush(outputStream)
        }
    }

    /**
     * 靠右一行文字顺序打印,支持不同字号
     *
     * @see IPrintable
     * @property textArray Array<String>              待打印文案数组
     * @property fontSizes Array<RelativeFontSize>    相对字体大小数组
     */
    class FontsRight(val textArray: Array<String>, private val fontSizes: Array<RelativeFontSize>) : IPrintable {
        init {
            if (textArray.size != fontSizes.size) throw RuntimeException("文字数组和字体数组的length不一致！")
        }

        override fun phonePrint(outputStream: OutputStream) {
            outputStream.write(ALIGN_RIGHT)
            textArray.forEachIndexed { index, text ->
                initFontSize(outputStream, fontSizes[index])
                writeText(outputStream, text)
            }
            breakLineFlush(outputStream)
        }
    }

    /**
     * 打印左右分开对齐的文案
     *
     * @see IPrintable
     * @property left String                  待打印左边文案
     * @property right String                 待打印右边文案
     * @property fontSize RelativeFontSize    相对字体大小
     */
    class LeftRight(val left: String, val right: String, val fontSize: RelativeFontSize = RelativeFontSize.Relative1) :
            IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            val length = 1.0 * DEFAULT_LENGTH //一行能打印的英文数 中文是它的一半
            val leftLength = UtilPrint.getGBKLength(left)
            val rightLength = UtilPrint.getGBKLength(right)
            val rightPart = (length * Math.ceil((leftLength + rightLength) / length) - leftLength).toInt()
            val text = UtilPrint.addPrintSpace(left, leftLength, 0) + UtilPrint.addPrintSpace(this.right, rightPart, 1)
            initFontSize(outputStream, fontSize)
            outputStream.write(ALIGN_LEFT)
            writeText(outputStream, text)
            breakLineFlush(outputStream)
        }
    }

    /**
     * 打印表格型
     *
     * @see IPrintable
     * @property texts Array<String>          文案数组
     * @property lengths IntArray             对应位置的长度
     * @property fontSize RelativeFontSize    文字的相对字号
     */
    class MultiPart(
            private val texts: Array<String>,
            private val lengths: IntArray,
            private val fontSize: RelativeFontSize = RelativeFontSize.Relative1
    ) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            if (texts.size != lengths.size) throw RuntimeException("文字数组和长度数组的length不一致！")
            var rightPartLength = DEFAULT_LENGTH
            val sumLength = lengths.sum()
            for (i in 0 until lengths.size) {
                if (i == lengths.size - 1) {
                    this.lengths[i] = rightPartLength
                } else {
                    this.lengths[i] = DEFAULT_LENGTH * lengths[i] / sumLength
                    rightPartLength -= this.lengths[i]
                }
            }

            val subTexts = arrayListOf<ArrayList<String>>()
            var maxLineCount = 0
            for (i in 0 until texts.size) {
                subTexts.add(UtilPrint.splitByByteLength(this.texts[i], lengths[i] - 1))
                val lineCount = subTexts[i].size
                if (maxLineCount < lineCount) {
                    maxLineCount = lineCount
                }
            }

            for (i in 0 until maxLineCount) {
                var lineContent = ""
                for (j in 0 until texts.size) {
                    var text = ""
                    if (subTexts[j].size > i) {
                        text = subTexts[j][i]
                    }
                    lineContent += if (j == 0) UtilPrint.addPrintSpace(text, lengths[j], 0)
                    else UtilPrint.addPrintSpace(text, lengths[j], 1)
                }
                initFontSize(outputStream, fontSize)
                outputStream.write(ALIGN_LEFT)
                writeText(outputStream, lineContent)
                breakLineFlush(outputStream)
            }
        }
    }

    /**
     * 打印分割线
     *
     * @see IPrintable
     * @property fontSize RelativeFontSize   相对字号
     */
    class Line(private val fontSize: RelativeFontSize = RelativeFontSize.Relative0) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            val space = when (findPhoneAbsoluteFontSize(fontSize)) {
                PHONE_SIZE_0 -> "-"// 英文符号 打出来是虚线
                PHONE_SIZE_1 -> "_"
                PHONE_SIZE_2 -> "*"
                PHONE_SIZE_3 -> "#"
                else -> "-"
            }
            val text = UtilPrint.addPrintSpace("", DEFAULT_LENGTH, 0, space)
            initFontSize(outputStream, RelativeFontSize.Relative0)
            outputStream.write(ALIGN_LEFT)
            writeText(outputStream, text)
            breakLineFlush(outputStream)
        }
    }

    /**
     * 打印空白行
     *
     * @see IPrintable
     * @property lineCount Int   打印行数
     * @property space String    空白字符
     */
    class BreakLine(private val lineCount: Int = 3, private val space: String = "") : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
            for (i in 0 until lineCount) {
                outputStream.write(NORMAL)
                writeText(outputStream, "$space\n")
            }
            breakLineFlush(outputStream)
        }
    }

    /**
     * 打印图片
     *
     * @see IPrintable
     * @property bitmap Bitmap   待打印位图
     * @property left Int        靠左偏移
     */
    class Image(val bitmap: Bitmap, val left: Int) : IPrintable {

        override fun phonePrint(outputStream: OutputStream) {
        }
    }

    companion object {
        var baseAbsFontSize: AbsoluteFontSize = AbsoluteFontSize.SIZE1   // 基准字号

        private const val DEFAULT_LENGTH = 32                            // 蓝牙打印时，行最大字符个数

        // ------------ PHONE START -----------
        private val RESET = byteArrayOf(0x1b, 0x40)                        // 复位打印机
        private val ALIGN_LEFT = byteArrayOf(0x1b, 0x61, 0x00)             // 左对齐
        private val ALIGN_CENTER = byteArrayOf(0x1b, 0x61, 0x01)           // 中间对齐
        private val ALIGN_RIGHT = byteArrayOf(0x1b, 0x61, 0x02)            // 右对齐
        private val BOLD = byteArrayOf(0x1b, 0x45, 0x01)                   // 选择加粗模式
        private val BOLD_CANCEL = byteArrayOf(0x1b, 0x45, 0x00)            // 取消加粗模式
        private val DOUBLE_HEIGHT_WIDTH = byteArrayOf(0x1d, 0x21, 0x11)    // 宽高加倍
        private val DOUBLE_WIDTH = byteArrayOf(0x1d, 0x21, 0x10)           // 宽加倍
        private val DOUBLE_HEIGHT = byteArrayOf(0x1d, 0x21, 0x01)          // 高加倍
        private val NORMAL = byteArrayOf(0x1d, 0x21, 0x00)                 // 字体不放大
        private val LINE_SPACING_DEFAULT = byteArrayOf(0x1b, 0x32)         // 设置默认行间距
        // ------------ PHONE END -----------

        // 普通设备的相对字号
        private val PHONE_SIZE_0 = PhoneAbsoluteFontSize(32, arrayOf(NORMAL, BOLD_CANCEL))
        private val PHONE_SIZE_1 = PhoneAbsoluteFontSize(32, arrayOf(NORMAL, BOLD_CANCEL))
        private val PHONE_SIZE_2 = PhoneAbsoluteFontSize(16, arrayOf(DOUBLE_HEIGHT, BOLD_CANCEL))
        private val PHONE_SIZE_3 = PhoneAbsoluteFontSize(16, arrayOf(DOUBLE_HEIGHT_WIDTH, BOLD_CANCEL))

        // ------------  PHONE START -----------
        /**
         * 普通设备打印文案
         *
         * @param outputStream OutputStream    打印流
         * @param text String                  待打印文案
         */
        fun writeText(outputStream: OutputStream, text: String) {
            val bytes = string2Bytes(text)
            outputStream.write(bytes, 0, bytes.size)
        }

        /**
         * 打印结束复位
         *
         * @param outputStream OutputStream    打印流
         */
        private fun breakLineFlush(outputStream: OutputStream) {
            writeText(outputStream, "\n")
            outputStream.write(NORMAL)
            outputStream.write(ALIGN_LEFT)
            outputStream.flush()
        }

        /**
         * 将字符串转为byte数组
         *
         * @param text String   指定字符串
         * @return ByteArray    目标byte数组
         */
        private fun string2Bytes(text: String) = text.toByteArray(Charset.forName("GBK"))

        /**
         * 根据相对字号设置字体大小
         *
         * @param outputStream OutputStream    打印流
         * @param fontSize RelativeFontSize    相对字号
         */
        private fun initFontSize(outputStream: OutputStream, fontSize: RelativeFontSize) {
            findPhoneAbsoluteFontSize(fontSize).array.forEach { outputStream.write(it) }
        }
        // ------------  PHONE END -----------

        /**
         * 普通设备根据相对字号获得字体大小
         *
         * @param size RelativeFontSize    相对字号
         * @return LklAbsoluteFontSize     普通设备字体大小
         */
        private fun findPhoneAbsoluteFontSize(size: RelativeFontSize): PhoneAbsoluteFontSize {
            return when (size.getAbsoluteFontSize(baseAbsFontSize)) {
                AbsoluteFontSize.SIZE0 -> PHONE_SIZE_0
                AbsoluteFontSize.SIZE1 -> PHONE_SIZE_1
                AbsoluteFontSize.SIZE2 -> PHONE_SIZE_2
                AbsoluteFontSize.SIZE3 -> PHONE_SIZE_3
            }
        }

        /**
         * 普通设备字体大小封装
         *
         * @property sizeCmd Int     当前字号尺寸
         * @property maxLength Int   该字号的最大尺寸
         */
        data class PhoneAbsoluteFontSize(val sizeCmd: Int, val array: Array<ByteArray>)

        /**
         * 字体大小
         */
        enum class AbsoluteFontSize {
            SIZE0,
            SIZE1,
            SIZE2,
            SIZE3
        }

        /**
         * 字体相对大小
         */
        enum class RelativeFontSize {
            Relative0,
            Relative1,
            Relative2,
            Relative3;

            /**
             * 根据基准字号获得相对的字号
             *
             * @param baseAbsFontSize AbsoluteFontSize    基准字号
             * @return AbsoluteFontSize                   相对字号
             */
            fun getAbsoluteFontSize(baseAbsFontSize: AbsoluteFontSize): AbsoluteFontSize {
                val absFontSizes = AbsoluteFontSize.values().size
                val calFontSizeOrdinal = baseAbsFontSize.ordinal + ordinal
                val absOrdinal = Math.min(calFontSizeOrdinal, absFontSizes - 1)
                return AbsoluteFontSize.values()[absOrdinal]
            }
        }
    }
}


