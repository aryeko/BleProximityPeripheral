package com.kogan.arye.proximityperipheral

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.kogan.arye.proximityperipheral.GattServices.ProximityGattService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AnkoLogger {

    private val REQUEST_ENABLE_BT: Int = 1
    private lateinit var mBlePeripheral : BlePeripheral
    private lateinit var mBluetoothManager : BluetoothManager
    private lateinit var mBluetoothAdapter : BluetoothAdapter
    private val mProximityGattService : ProximityGattService = ProximityGattService()

    var dummy = "A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter

        if(!mBluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            //TODO: Fix app crush
        }

        mBlePeripheral = BlePeripheral(this, mBluetoothAdapter, mBluetoothManager)
        mBlePeripheral.addGattService(mProximityGattService)

        info { "ProximityPeripheral has created" }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_ENABLE_BT && resultCode != Activity.RESULT_OK)
        {
            error { "Bluetooth is not enabled" }
            //TODO: Wait for bluetooth to be enabled
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        //Reduced power on background
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_startAdv -> {
                mBlePeripheral.startAdvertising()
            }
            R.id.nav_stopAdv -> {
                mBlePeripheral.stopAdvertising()
            }
            R.id.nav_change_value -> {
                info{"Set proximity state to: $dummy"}
                mProximityGattService.setProximityValue(dummy)
                dummy = dummy.repeat(2)
            }
            R.id.nav_manage -> {
                mBlePeripheral.notifyCharacteristicChanged(mProximityGattService)
            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }



}
