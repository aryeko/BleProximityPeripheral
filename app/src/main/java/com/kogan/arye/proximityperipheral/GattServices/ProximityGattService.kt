package com.kogan.arye.proximityperipheral.GattServices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import java.util.*

/**
 * Created by aryekoga on 4/3/2018.
 */
class ProximityGattService : IGattService {

    private val mProximityCharacteristicUuid: UUID = UUID.fromString("08590F7E-DB05-467E-8757-72F6FAEB13D4")
    private val CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")
    private val CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    private val mProximityCharacteristic : BluetoothGattCharacteristic
    private val mBluetoothGattService : BluetoothGattService

    init {
        mProximityCharacteristic = BluetoothGattCharacteristic(mProximityCharacteristicUuid,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ)

        val userDescriptor = BluetoothGattDescriptor(CHARACTERISTIC_USER_DESCRIPTION_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        userDescriptor.value = "Proximity".toByteArray(Charsets.UTF_8)
        mProximityCharacteristic.addDescriptor(userDescriptor)

        val confDescriptor = BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        confDescriptor.value = "Conf".toByteArray(Charsets.UTF_8)
        mProximityCharacteristic.addDescriptor(confDescriptor)

        mBluetoothGattService = BluetoothGattService(serviceUUID.uuid,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)
        mBluetoothGattService.addCharacteristic(mProximityCharacteristic)
    }

    override val serviceUUID: ParcelUuid
        get() = ParcelUuid(UUID(0, 0x2711))

    override val advertiseSettings: AdvertiseSettings
        get() = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

    override val advertiseData: AdvertiseData
        get() = AdvertiseData.Builder()
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(this.serviceUUID)
                .build()

    override val scanResponse: AdvertiseData
        get() = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()

    override fun getBluetoothGattService(): BluetoothGattService {
        return mBluetoothGattService
    }

    override fun toString(): String {
        return "ProximityGattService - UUID [$serviceUUID]"
    }

    fun setProximityValue(value : String){
        mProximityCharacteristic.setValue(value)
    }
}