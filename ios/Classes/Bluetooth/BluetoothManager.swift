//
//  BluetoothManager.swift
//  takeaway
//
//  Created by 徐强强 on 2018/4/9.
//  Copyright © 2018年 zaihui. All rights reserved.
//

import UIKit
import CoreBluetooth


/// 蓝牙操作阶段
///
/// - failed: 连接失败
/// - connect: 蓝牙连接阶段
/// - seekService: 搜索服务阶段
/// - seekCharacteristic: 搜索特性阶段
/// - seekdescription: 搜索描述信息阶段
enum BluetoothOptionStage {
    case failed
    case connect
    case seekServices
    case seekCharacteristics
    case seekDescriptors
}

//  蓝牙状态改变的block
typealias BluetoothStateUpdateBlock = ((_ central: CBCentralManager) -> ())

//  发现一个蓝牙外设的block
typealias BluetoothDiscoverPeripheralBlock = ((_ central: CBCentralManager, _ peripheral: CBPeripheral, _ advertisementData: [String: Any], _ RSSI: NSNumber) -> ())

//  连接完成的block,失败error就不为nil
typealias BluetoothConnectCompletionBlock = ((_ peripheral: CBPeripheral, _ error: Error?) -> ())

//  搜索到连接上的蓝牙外设的服务block
typealias BluetoothDiscoveredServicesBlock = ((_ peripheral: CBPeripheral, _ services: [CBService]?, _ error: Error?) -> ())

//  搜索某个服务的子服务的回调
typealias BluetoothDiscoveredIncludedServicesBlock = ((_ peripheral: CBPeripheral, _ service: CBService, _ includedServices: [CBService]?, _ error: Error?) -> ())

//  搜索到某个服务中的特性的block
typealias BluetoothDiscoverCharacteristicsBlock = ((_ peripheral: CBPeripheral, _ service: CBService, _ characteristics: [CBCharacteristic]?, _ error: Error?) -> ())

//  收到某个特性值更新的回调
typealias BluetoothNotifyCharacteristicBlock = ((_ peripheral: CBPeripheral, _ characteristic: CBCharacteristic, _ error: Error?) -> ())

//  查找到某个特性的描述 block
typealias BluetoothDiscoverDescriptorsBlock = ((_ peripheral: CBPeripheral, _ characteristic: CBCharacteristic, _ descriptors: [CBDescriptor], _ error: Error?) -> ())

//  统一返回使用的block
typealias BluetoothCompletionBlock = ((_ stage: BluetoothOptionStage, _ peripheral: CBPeripheral, _ service: CBService?, _ characteristic: CBCharacteristic?, _ error: Error?) -> ())

//  获取特性中的值
typealias BluetoothValueForCharacteristicBlock = ((_ characteristic: CBCharacteristic, _ value: Data?, _ error: Error?) -> ())

//  获取描述中的值
typealias BluetoothValueForDescriptorBlock = (( _ descriptor: CBDescriptor, _ value: Data?, _ error: Error?) -> ())

//  往特性中写入数据的回调
typealias BluetoothWriteToCharacteristicBlock = ((_ characteristic: CBCharacteristic, _ error: Error?) -> ())

//  往描述中写入数据的回调
typealias BluetoothWriteToDescriptorBlock = ((_ descriptor: CBDescriptor, _ error: Error?) -> ())

//  获取蓝牙外设信号的回调
typealias BluetoothGetRSSIBlock = ((_ peripheral: CBPeripheral, _ RSSI: NSNumber, _ error: Error?) -> ())


class BluetoothManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    
    static let shared = BluetoothManager()
    
    // 蓝牙模块状态改变的回调
    var stateUpdateBlock: BluetoothStateUpdateBlock?
    
    // 发现一个蓝牙外设的回调
    var discoverPeripheralBlcok: BluetoothDiscoverPeripheralBlock?
    
    // 连接断开的回调（蓝牙外设端的主动断开）
    var disconnectPeripheralBlock:BluetoothConnectCompletionBlock?
    
    // 连接外设完成的回调
    var connectCompleteBlock: BluetoothConnectCompletionBlock?
    
    // 发现服务的回调
    var discoverServicesBlock: BluetoothDiscoveredServicesBlock?
    
    // 发现服务中的特性的回调
    var discoverCharacteristicsBlock: BluetoothDiscoverCharacteristicsBlock?
    
    // 特性值改变的回调
    var notifyCharacteristicBlock: BluetoothNotifyCharacteristicBlock?
    
    // 发现服务中的子服务的回调
    var discoverdIncludedServicesBlock: BluetoothDiscoveredIncludedServicesBlock?
    
    // 发现特性的描述的回调
    var discoverDescriptorsBlock: BluetoothDiscoverDescriptorsBlock?
    
    // 操作完成的统一回调
    var completionBlock: BluetoothCompletionBlock?
    
    // 获取特性值回调
    var valueForCharacteristicBlock: BluetoothValueForCharacteristicBlock?
    
    // 获取描述值的回调
    var valueForDescriptorBlock: BluetoothValueForDescriptorBlock?
    
    // 将数据写入特性中的回调
    var writeToCharacteristicBlock: BluetoothWriteToCharacteristicBlock?
    
    // 将数据写入描述中的回调
    var writeToDescriptorBlock: BluetoothWriteToDescriptorBlock?
    
    // 获取蓝牙外设信号强度的回调
    var getRSSIBlock: BluetoothGetRSSIBlock?
    
    // 设备列表发生变化
    var deviceListChangedBlock: VoidBlock?
    
    // 当前连接的外设
    var connectedPerpheral: CBPeripheral?
    
    // 每次发送的最大数据长度，因为部分型号的蓝牙打印机一次写入数据过长，会导致打印乱码。
    private let limitLength: Int = 32
    
    // 中心管理器
    private var centralManager: CBCentralManager!
    
    // 查找到的设备列表
    private var deviceList: [CBPeripheral] = [] {
        didSet {
            deviceListChangedBlock?()
        }
    }
    
    // 要查找服务的UUIDs
    private var serviceUUIDs: [CBUUID]?
    
    // 要查找特性的UUIDs
    private var characteristicUUIDs: [CBUUID]?
    
    // 是否连接成功后停止扫描蓝牙设备
    private var isStopScanAfterConnected: Bool = false
    
    // 写入次数
    private var writeCount: Int = 0
    
    // 返回次数
    private var responseCount: Int = 0
    
    // 是否调用断开连接的block
    var isResponedDisconnectedBlock: Bool = false
    
    override init() {
        super.init()
        centralManager = CBCentralManager.init(delegate: self,
                              queue: DispatchQueue.main,
                              options: [CBCentralManagerOptionShowPowerAlertKey : NSNumber(value: true)])
    }
    
    // MARK: -- 蓝牙服务
    
    /// 开始搜索蓝牙外设，每次在block中返回一个蓝牙外设信息
    ///
    /// - Parameters:
    ///   - uuids: 服务的CBUUID
    ///   - options: 其他可选参数
    ///   - discoverBlock: 搜索到蓝牙外设后的回调
    func scanForPeripherals(withServices uuids: [CBUUID]? = nil, options: [String : Any]? = nil, didDiscoverPeripheral discoverBlock: BluetoothDiscoverPeripheralBlock? = nil) {
        if discoverBlock != nil {
            self.discoverPeripheralBlcok = discoverBlock
        }
        self.centralManager.scanForPeripherals(withServices: uuids, options: options)
    }
    
    
    /// 连接某个蓝牙外设，并查询服务，特性，特性描述
    ///
    /// - Parameters:
    ///   - peripheral: 要连接的蓝牙外设
    ///   - connectOptions: 连接的配置参数
    ///   - isStopScanAfterConnected: 连接成功后是否停止搜索蓝牙外设
    ///   - serviceUUIDs: 要搜索的服务UUID
    ///   - characteristicUUIDs: 要搜索的特性UUID
    ///   - completionBlock: 操作执行完的回调
    func connectPeripheral(_ peripheral: CBPeripheral,
                           connectOptions: [String : Any]?,
                           isStopScanAfterConnected: Bool = true,
                           serviceUUIDs: [CBUUID]? = nil,
                           characteristicUUIDs: [CBUUID]? = nil,
                           completionBlock: BluetoothCompletionBlock? = nil) {
        if completionBlock != nil {
            self.completionBlock = completionBlock
        }

        self.serviceUUIDs = serviceUUIDs
        self.characteristicUUIDs = characteristicUUIDs
        self.isStopScanAfterConnected = isStopScanAfterConnected
        
        // 先取消之前连接的蓝牙外设
        cancelPeripheralConnection()
        
        // 开始连接新的蓝牙外设
        centralManager.connect(deviceList.first(where: { $0.name == peripheral.name }) ?? peripheral, options: connectOptions)
        peripheral.delegate = self
    }
    
    
    /// 查找某个服务的子服务
    ///
    /// - Parameters:
    ///   - includedServicesUUIDs: 要查找的子服务的UUIDs
    ///   - service: 父服务
    func discoverIncludedServices(_ includedServicesUUIDs: [CBUUID]?, for service: CBService) {
        connectedPerpheral?.discoverIncludedServices(includedServicesUUIDs, for: service)
    }
    
    /// 读取某个特性的值
    ///
    /// - Parameters:
    ///   - characteristic: 要读取的特性
    ///   - completionBlock: 读取完后的回调
    func readValue(for characteristic: CBCharacteristic, withCompletionBlock completionBlock: BluetoothValueForCharacteristicBlock?) {
        valueForCharacteristicBlock = completionBlock
        connectedPerpheral?.readValue(for: characteristic )
    }
    
    
    /// 往某个特性中写入数据
    ///
    /// - Parameters:
    ///   - data: 写入的数据
    ///   - characteristic: 特性对象
    ///   - type: 写入类型
    ///   - completionBlock: 写入完成后的回调,只有type为withResponse时，才会回调
    func writeValue(_ data: Data,
                    for characteristic: CBCharacteristic,
                    type: CBCharacteristicWriteType,
                    withCompletionBlock completionBlock: BluetoothWriteToCharacteristicBlock?) {
        writeToCharacteristicBlock = completionBlock
        writeCount = 0
        responseCount = 0
        print(data.count)
        if let connectedPerpheral = self.connectedPerpheral {
            
            if data.count <= limitLength {
                connectedPerpheral.writeValue(data, for: characteristic, type: type)
                writeCount += 1
            } else {
                writeCount = data.count % limitLength == 0 ? data.count / limitLength : data.count / limitLength + 1
                for index in 0..<writeCount {
                    if limitLength * (index + 1) <= data.count {
                        let subData = data.subdata(in: (index * limitLength)..<(index + 1) * limitLength)
                        connectedPerpheral.writeValue(subData, for: characteristic, type: type)
                    } else {
                        let subData = data.subdata(in: (index * limitLength)..<data.count)
                        connectedPerpheral.writeValue(subData, for: characteristic, type: type)
                    }
                }
            }
        }
    }
    
    
    /// 读取某特性的描述信息
    ///
    /// - Parameters:
    ///   - descriptor: 描述对象
    ///   - completionBlock: 读取结果返回时的回调
    func readValue(for descriptor: CBDescriptor, withCompletionBlock completionBlock: BluetoothValueForDescriptorBlock?) {
        valueForDescriptorBlock = completionBlock
        connectedPerpheral?.readValue(for: descriptor)
    }
    
    
    /// 将数据写入特性的描述中
    ///
    /// - Parameters:
    ///   - data: 数据
    ///   - descriptor: 描述对象
    ///   - completionBlock: 数据写入完成后的回调
    func writeValue(_ data: Data,
                    for descriptor: CBDescriptor,
                    withCompletionBlock completionBlock: BluetoothWriteToDescriptorBlock?) {
        writeToDescriptorBlock = completionBlock
        connectedPerpheral?.writeValue(data, for: descriptor)
    }
    
    
    /// 获取某外设的信号
    ///
    /// - Parameter completionBlock: 获取信号完成后的回调
    func readRSSI(withCompletionBlock completionBlock: BluetoothGetRSSIBlock?) {
        getRSSIBlock = completionBlock
        connectedPerpheral?.readRSSI()
    }
    
    // 停止扫描
    func stopScan() {
        centralManager.stopScan()
    }
    
    // 断开蓝牙连接
    func cancelPeripheralConnection() {
        if let connectedPerpheral = self.connectedPerpheral {
            centralManager.cancelPeripheralConnection(connectedPerpheral)
        }
    }
    
    // MARK: -- CBCentralManagerDelegate
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        stateUpdateBlock?(central)
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if let samePeripheralIndex = deviceList.enumerated().first(where: { $0.element.name == peripheral.name })?.offset {
            deviceList[samePeripheralIndex] = peripheral
        } else {
            if !(peripheral.name ?? "").isEmpty {
                deviceList.append(peripheral)
            }
        }
        discoverPeripheralBlcok?(central, peripheral, advertisementData, RSSI)
    }
    
    // MARK: -- 连接外设成功和失败的代理
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        isResponedDisconnectedBlock = false
        connectedPerpheral = peripheral
        deviceList = deviceList.map {
            if $0.name == peripheral.name {
                return peripheral
            }
            return $0
        }
        connectedPerpheral?.delegate = self
        if isStopScanAfterConnected {
            centralManager.stopScan()
        }
        discoverServicesBlock?(peripheral, peripheral.services, nil)
        completionBlock?(.connect, peripheral, nil, nil, nil)
        peripheral.discoverServices(serviceUUIDs)
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        completionBlock?(.failed, peripheral, nil, nil, error)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        connectedPerpheral = nil
        deviceList = deviceList.map {
            if $0.name == peripheral.name {
                return peripheral
            }
            return $0
        }
        if !isResponedDisconnectedBlock {
            disconnectPeripheralBlock?(peripheral, error)
        }
        print("断开连接了\(error.debugDescription)")
    }
    
    // MARK: -- 发现服务的代理
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        completionBlock?(.seekServices, peripheral, nil, nil, error)
        if error == nil {
            peripheral.services?.forEach({
                peripheral.discoverCharacteristics(self.characteristicUUIDs, for: $0)
            })
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
        discoverdIncludedServicesBlock?(peripheral, service, service.includedServices, nil)
    }
    
    // MARK: -- 服务特性的代理
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        completionBlock?(.seekCharacteristics, peripheral, service, nil, error)
        if error == nil {
            discoverCharacteristicsBlock?(peripheral, service, service.characteristics, error)
            service.characteristics?.forEach({
                peripheral.discoverDescriptors(for: $0)
                peripheral.readValue(for: $0)
            })
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        notifyCharacteristicBlock?(peripheral, characteristic, error)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        valueForCharacteristicBlock?(characteristic, characteristic.value, error)
    }
    
    // MARK: -- 发现服务特性描述的代理
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
        completionBlock?(.seekDescriptors, peripheral, nil, characteristic, error)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor descriptor: CBDescriptor, error: Error?) {
        valueForDescriptorBlock?(descriptor, descriptor.value as? Data, error)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor descriptor: CBDescriptor, error: Error?) {
        writeToDescriptorBlock?(descriptor, error)
    }
    
    // MARK: -- 写入数据的回调
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if writeToCharacteristicBlock != nil {
            responseCount += 1
            if writeCount == responseCount {
                writeToCharacteristicBlock?(characteristic, error)
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didReadRSSI RSSI: NSNumber, error: Error?) {
        getRSSIBlock?(peripheral, RSSI, error)
    }
    
    // MARK: -- 公有方法
    
    func isBluetoothPoweredOff() -> Bool {
        if centralManager.state == .poweredOff {
            return true
        }
        return false
    }
    
    func getDeviceList() -> [CBPeripheral] {
        return deviceList
    }
}
