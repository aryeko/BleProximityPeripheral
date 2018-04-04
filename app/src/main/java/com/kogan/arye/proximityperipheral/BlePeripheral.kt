package com.kogan.arye.proximityperipheral

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import com.kogan.arye.proximityperipheral.GattServices.IGattService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.nio.charset.Charset

/**
 * Created by aryekoga on 4/3/2018.
 */
class BlePeripheral(val mAppContext: Context,
                    val mBluetoothAdapter: BluetoothAdapter,
                    val mBluetoothManager: BluetoothManager) : AnkoLogger{

    private var mGattServicesList : MutableList<IGattService> = mutableListOf()
    private var mGattServer: BluetoothGattServer
    private val mServerCallback: BluetoothGattServerCallback
    private var mCurrentlyAdvertising : Boolean

    init {
        mServerCallback = PeripheralGattServerCallback()
        mGattServer = mBluetoothManager.openGattServer(
                mAppContext,
                mServerCallback
        )
        mCurrentlyAdvertising = false
    }

    fun addGattService(gattService: IGattService) {
        mGattServicesList.add(gattService)
        //TODO: check if GATT service is already there before adding it
        mGattServer.addService(gattService.getBluetoothGattService())
    }

    fun notifyCharacteristicChanged(gattService: IGattService) {
        val characteristic = gattService.getBluetoothGattService().getCharacteristic(gattService.serviceUUID.uuid) //TODO: Wrong UUID, fix it!
        val indicate = true //TODO: set indicate\notify correctly
        for (device in mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT)) {
            // true for indication (acknowledge) and false for notification (unacknowledge).
            info {
                "${if (indicate) {
                    "Indicating"
                } else {
                    "Notifying"
                }} device [$device] for characteristic changed (characteristic - $characteristic)"
            }
            mGattServer.notifyCharacteristicChanged(device, characteristic, indicate)
        }
    }

    fun startAdvertising(){
        mGattServicesList.forEach({s ->
            info {"Start advertising for service: $s" }
            mBluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(s.advertiseSettings, s.advertiseData, s.scanResponse, mAdvCallback)})

        mCurrentlyAdvertising = true
    }

    fun stopAdvertising(){
        info { "Stop advertising" }
        mBluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(mAdvCallback)
    }

    private val mAdvCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            when(errorCode){
                ADVERTISE_FAILED_ALREADY_STARTED -> info { "Start advertising failed with the error [ADVERTISE_FAILED_ALREADY_STARTED]" }
                ADVERTISE_FAILED_DATA_TOO_LARGE -> info { "Start advertising failed with the error [ADVERTISE_FAILED_DATA_TOO_LARGE]" }
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> info { "Start advertising failed with the error [ADVERTISE_FAILED_FEATURE_UNSUPPORTED]" }
                ADVERTISE_FAILED_INTERNAL_ERROR -> info { "Start advertising failed with the error [ADVERTISE_FAILED_INTERNAL_ERROR]" }
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> info { "Start advertising failed with the error [ADVERTISE_FAILED_TOO_MANY_ADVERTISERS]" }
            }
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            info { "Successfully started to advertise with the following settings: $settingsInEffect" }
        }
    }

    inner class PeripheralGattServerCallback : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            info { "onCharacteristicReadRequest - device=$device, requestId=$requestId, offset=$offset, characteristic=$characteristic" }
            info { "characteristic value: ${characteristic?.value?.toString(Charset.defaultCharset())}" }

            /**
             * Reading remote device's RSSI (signal strength) value and responding with it
             *
             * TODO: Move to the ProximityGattService
             */
            device!!.connectGatt(mAppContext, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    info { "Connection status changed" }
                    gatt!!.readRemoteRssi()
                    info { "Read remote RSSI request sent." }
                }
                override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                    super.onReadRemoteRssi(gatt, rssi, status)
                    info { "Remote RSSI is: $rssi, status is: $status" }
                    info { "Responding with GATT success and RSSI value: $rssi" }
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, byteArrayOf(rssi.toByte()))
                }
            })
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            info { "onCharacteristicWriteRequest - device=$device, requestId=$requestId, characteristic=$characteristic, preparedWrite=$preparedWrite, responseNeeded=$responseNeeded, offset=$offset, value=$value" }
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            info { "onConnectionStateChange" }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            info { "onDescriptorReadRequest - descriptor=$descriptor" }
            info { "descriptor value: ${descriptor?.value}" }

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor?.value)
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            info { "onDescriptorWriteRequest" }
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
            info { "onExecuteWrite" }
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            info { "onMtuChanged" }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            info { "onNotificationSent" }
        }

        override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(device, txPhy, rxPhy, status)
            info { "onPhyRead" }
        }

        override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(device, txPhy, rxPhy, status)
            info { "onPhyUpdate" }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            info { "onServiceAdded - status=$status, service=$service" }
        }
    }
}