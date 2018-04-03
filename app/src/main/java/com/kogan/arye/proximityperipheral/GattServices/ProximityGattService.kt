package com.kogan.arye.proximityperipheral.GattServices

import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import java.util.*

/**
 * Created by aryekoga on 4/3/2018.
 */
class ProximityGattService : IGattService {
    override val serviceUUID: ParcelUuid
        get() = ParcelUuid(UUID(0, 2711))

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return "${this::class.simpleName} - UUID [$serviceUUID]"
    }
}