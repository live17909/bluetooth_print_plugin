//
//  BluetoothPrinter.swift
//  takeaway
//
//  Created by 徐强强 on 2018/4/10.
//  Copyright © 2018年 zaihui. All rights reserved.
//

import Foundation


// 文字对齐方式
enum PrinterTextAlignment: UInt8 {
    case left = 0x00
    case center = 0x01
    case right = 0x02
}

// 字号
enum PrinterFontSize: UInt8 {
    case normal = 0x00
    case doubleWidth = 0x10
    case doubleHeight = 0x01
    case double = 0x11
}

enum PrinterBoldFont: UInt8 {
    case normal = 0x00
    case bold = 0x01
}

class BluetoothPrinter {
    
    static let enc = String.Encoding(rawValue: CFStringConvertEncodingToNSStringEncoding(0x0632))
    
    var printerData: Data = Data()
    
    var rowLimitLength: Int = 32
    
    var fontSpace: Int = 2
    
    var spaceCount = 0

    let leftTextProportion: Double = 5.0 / 8.0
    
    let middleTextProportion: Double = 1.0 / 8.0
    
    let rightTextProportion: Double = 2 / 8.0
    
    let printerSpace = " "

    init() {
        defaultSetting()
    }
    
    private func defaultSetting() {
        
        // 1.初始化打印机
        printerData.append(Data(bytes: [0x1B, 0x40]))
        
        // 2.设置行间距为1/6英寸，约34个点
        printerData.append(Data(bytes: [0x1B, 0x32]))
        
        // 3.设置字体:标准0x00，压缩0x01;
        printerData.append(Data(bytes: [0x1B, 0x4D, 0x00]))
    
    }
    
    // MARK: -- 基本操作
    
    // 换行
    func appendNewLine() {
        setFontSize(.normal)
        printerData.append(Data(bytes: [0x0A]))
    }
    
    /// 设置对齐方式
    ///
    /// - Parameter alignment: 对齐方式
    private func setAlignment(_ alignment: PrinterTextAlignment) {
        printerData.append(Data(bytes: [0x1B, 0x61, alignment.rawValue]))
    }
    
    
    /// 设置字体大小
    ///
    /// - Parameter fontSize: 字号
    private func setFontSize(_ fontSize: PrinterFontSize, boldFont: PrinterBoldFont = .normal) {
        printerData.append(Data(bytes: [0x1d, 0x21, fontSize.rawValue]))
        printerData.append(Data.init(bytes: [0x1b, 0x45, boldFont.rawValue]))
    }
    
    /// 添加文字，不换行
    ///
    /// - Parameters:
    ///   - text: 文字内容
    ///   - withLimitLength: 最多可以允许多少个字节,后面加...
    private func setText(_ text: String, withLimitLength limitLength: Int = 0) {
        var tempText = text
        var data = tempText.data(using: BluetoothPrinter.enc) ?? Data()

        if limitLength > 0 {
            if data.count > limitLength {
                data = data.subdata(in: 0..<limitLength)
                tempText = String(data: data, encoding: BluetoothPrinter.enc) ?? ""
            }
            tempText.append("...")
            data = tempText.data(using: BluetoothPrinter.enc) ?? Data()
        }
        printerData.append(data)
    }
    
    // MARK: -- 拼接文字
    
    func append(text: String, alignment: PrinterTextAlignment = .left, fontSize: PrinterFontSize = .normal, boldFont: PrinterBoldFont = .normal, isAppendNewLine: Bool = true) {
        setAlignment(alignment)
        setFontSize(fontSize, boldFont: boldFont)
        setText(text)
        if isAppendNewLine {
            appendNewLine()
        }
    }
    
    func append(title: String, value: String, fontSize: PrinterFontSize = .normal, boldFont: PrinterBoldFont = .normal) {
        
        setAlignment(.left)
        setFontSize(fontSize, boldFont: boldFont)
        let titleLength = title.getPrinterCount()
        let valueLength = value.getPrinterCount()
        let spaceLength = rowLimitLength - titleLength - valueLength
        if spaceLength >= fontSpace {
            let space = printerSpace.repeatStr(count: spaceLength)
            setText(title + space + value)
        } else {
            setText(title)
            appendNewLine()
            setAlignment(.right)
            setText(value)
        }
        appendNewLine()
    }
    
    func append(leftText: String, middleText: String, rightText: String, fontSize: PrinterFontSize = .normal, boldFont: PrinterBoldFont = .normal) {
        setAlignment(.left)
        setFontSize(fontSize, boldFont: boldFont)
        let leftReserveLength = Int(Double(rowLimitLength) * leftTextProportion)
        let middleReserveLength = Int(Double(rowLimitLength) * middleTextProportion)
        let rightReserveLength = Int(Double(rowLimitLength) * rightTextProportion)
        let leftSapceLength = leftReserveLength - leftText.getPrinterCount()
        let middleSapceLength = middleReserveLength - middleText.getPrinterCount()
        let rightSapceLength = rightReserveLength - rightText.getPrinterCount()
        if leftSapceLength > 0 && middleSapceLength > 0 && rightSapceLength > 0 {
            var text = leftText
            text.append(printerSpace.repeatStr(count: leftSapceLength + middleSapceLength))
            text.append(middleText)
            text.append(printerSpace.repeatStr(count: rightSapceLength))
            text.append(rightText)
            setText(text)
        } else {
            setText(leftText)
            appendNewLine()
            if middleSapceLength > 0 && rightSapceLength > 0 {
                setFontSize(fontSize, boldFont: boldFont)
                var text = printerSpace.repeatStr(count: leftReserveLength + middleSapceLength)
                text.append(middleText)
                text.append(printerSpace.repeatStr(count: rightSapceLength))
                text.append(rightText)
                setText(text)
            } else {
                setAlignment(.right)
                setFontSize(fontSize, boldFont: boldFont)
                setText(middleText + printerSpace.repeatStr(count: 2) + rightText)
            }
        }
        appendNewLine()
    }
    
    // MARK: -- 拼接自定义的data
    
    func append(customData: Data) {
        if !customData.isEmpty {
            printerData.append(customData)
        }
    }
    
    // MARK: -- 拼接其他
    
    func appendAsteriskLine(with text: String = "") {
        setAlignment(.center)
        setFontSize(.normal)
        var line = ""
        if text.isEmpty {
            line = "*".repeatStr(count: rowLimitLength)
            setText(line)
        } else {
            let textLength = text.getPrinterCount()
            let asteriskLength = (rowLimitLength - textLength * 2)
            line.append("*".repeatStr(count: asteriskLength/2))
            setText(line)
            setFontSize(.double, boldFont: .bold)
            setText(text)
            setFontSize(.normal)
            line = "*".repeatStr(count: asteriskLength % 2 == 0 ? asteriskLength / 2 : (asteriskLength / 2 - 1))
            setText(line)
        }
        appendNewLine()
    }
    
    func appendSeperatorLine() {
        setAlignment(.center)
        setFontSize(.normal)
        let line = "-".repeatStr(count: rowLimitLength)
        let data = line.data(using: BluetoothPrinter.enc) ?? Data()
        printerData.append(data)
        appendNewLine()
    }
    
}
