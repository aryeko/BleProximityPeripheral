package com.kogan.arye.proximityperipheral.GattServices

import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid

/**
 * Created by aryekoga on 4/3/2018.
 */
interface IGattService {
    val serviceUUID: ParcelUuid
    val advertiseSettings: AdvertiseSettings
    val advertiseData: AdvertiseData
    val scanResponse: AdvertiseData

    fun getBluetoothGattService() : BluetoothGattService
}