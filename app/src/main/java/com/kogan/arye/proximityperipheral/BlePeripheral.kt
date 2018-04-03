package com.kogan.arye.proximityperipheral

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.util.Log
import com.kogan.arye.proximityperipheral.GattServices.IGattService

/**
 * Created by aryekoga on 4/3/2018.
 */
class BlePeripheral(val context: Context,
                    val mBluetoothAdapter: BluetoothAdapter,
                    val mBluetoothManager: BluetoothManager) {

    private var mGattServicesList : MutableList<IGattService> = mutableListOf()
    private var mGattServer: BluetoothGattServer
    private val mServerCallback: BluetoothGattServerCallback
    private var mCurrentlyAdvertising : Boolean

    init {
        mServerCallback = PeripheralGattServerCallback()
        mGattServer = mBluetoothManager.openGattServer(
                context,
                mServerCallback
        )
        mCurrentlyAdvertising = false
    }

    fun addGattService(gattService: IGattService) {
        mGattServicesList.add(gattService)
    }

    fun addGattServices(gattServices : MutableList<IGattService>){
        mGattServicesList.addAll(gattServices)
    }

    fun startAdvertising(){
        mGattServicesList.forEach({s ->
            Log.d(this::class.simpleName, "Start advertising for service: $s")
            mBluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(s.advertiseSettings, s.advertiseData, s.scanResponse, mAdvCallback)})

        mCurrentlyAdvertising = true
    }

    fun stopAdvertising(){
        Log.d(this::class.simpleName, "Stop advertising")
        mBluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(mAdvCallback)
    }

    private val mAdvCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            when(errorCode){
                ADVERTISE_FAILED_ALREADY_STARTED -> Log.d(this::class.simpleName, "Start advertising failed with the error [ADVERTISE_FAILED_ALREADY_STARTED]")
                ADVERTISE_FAILED_DATA_TOO_LARGE -> Log.d(this::class.simpleName, "Start advertising failed with the error [ADVERTISE_FAILED_DATA_TOO_LARGE]")
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> Log.d(this::class.simpleName, "Start advertising failed with the error [ADVERTISE_FAILED_FEATURE_UNSUPPORTED]")
                ADVERTISE_FAILED_INTERNAL_ERROR -> Log.d(this::class.simpleName, "Start advertising failed with the error [ADVERTISE_FAILED_INTERNAL_ERROR]")
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> Log.d(this::class.simpleName, "Start advertising failed with the error [ADVERTISE_FAILED_TOO_MANY_ADVERTISERS]")
            }
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(this::class.simpleName, "Successfully started to advertise with the following settings: $settingsInEffect")
        }
    }

    inner class PeripheralGattServerCallback : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.d(this::class.simpleName, "onCharacteristicReadRequest - device=$device, requestId=$requestId, offset=$offset, characteristic=$characteristic")
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            Log.d(this::class.simpleName, "onCharacteristicWriteRequest - device=$device, requestId=$requestId, characteristic=$characteristic, preparedWrite=$preparedWrite, responseNeeded=$responseNeeded, offset=$offset, value=$value")
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.d(this::class.simpleName, "onConnectionStateChange")
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            Log.d(this::class.simpleName, "onDescriptorReadRequest")
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            Log.d(this::class.simpleName, "onDescriptorWriteRequest")
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
            Log.d(this::class.simpleName, "onExecuteWrite")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Log.d(this::class.simpleName, "onMtuChanged")
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            Log.d(this::class.simpleName, "onNotificationSent")
        }

        override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(device, txPhy, rxPhy, status)
            Log.d(this::class.simpleName, "onPhyRead")
        }

        override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(device, txPhy, rxPhy, status)
            Log.d(this::class.simpleName, "onPhyUpdate")
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            Log.d(this::class.simpleName, "onServiceAdded - status=$status, service=$service")
        }
    }
}