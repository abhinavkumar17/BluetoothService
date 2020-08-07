package com.example.bluetoothservice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity


class DeviceDiscovery : AppCompatActivity()  {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var btnEnableDisable_Discoverable: Button? = null
    var mBTDevices: ArrayList<BluetoothDevice> = ArrayList()
    var mDeviceListAdapter: DeviceListAdapter? = null
    var lvNewDevices: ListView? = null


    // Create a BroadcastReceiver for ACTION_FOUND
    private val mBroadcastReceiver1: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF ->println(
                        "onReceive: STATE OFF"
                    )
                    BluetoothAdapter.STATE_TURNING_OFF ->println(
                        "mBroadcastReceiver1: STATE TURNING OFF"
                    )
                    BluetoothAdapter.STATE_ON ->println(
                        "mBroadcastReceiver1: STATE ON"
                    )
                    BluetoothAdapter.STATE_TURNING_ON -> println(
                        "mBroadcastReceiver1: STATE TURNING ON"
                    )
                }
            }
        }
    }

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private val mBroadcastReceiver2: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                val mode =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)
                when (mode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ->println(
                        "mBroadcastReceiver2: Discoverability Enabled."
                    )
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> println(
                        "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections."
                    )
                    BluetoothAdapter.SCAN_MODE_NONE ->println(
                        "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections."
                    )
                    BluetoothAdapter.STATE_CONNECTING -> println(
                        "mBroadcastReceiver2: Connecting...."
                    )
                    BluetoothAdapter.STATE_CONNECTED ->println(
                        "mBroadcastReceiver2: Connected."
                    )
                }
            }
        }
    }


    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private val mBroadcastReceiver3: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            println( "onReceive: ACTION FOUND.")
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                mBTDevices.add(device)
                println(
                    "onReceive: " + device.name + ": " + device.address
                )
                mDeviceListAdapter =
                    DeviceListAdapter(this@DeviceDiscovery, R.layout.device_adapter_view, mBTDevices)
                lvNewDevices?.adapter = mDeviceListAdapter
            }
        }
    }


    override fun onDestroy() {
        println("onDestroy: called.")
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver1)
        unregisterReceiver(mBroadcastReceiver2)
        //mBluetoothAdapter.cancelDiscovery();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discover_device)
        val btnONOFF: Button = findViewById(R.id.btnONOFF) as Button
        btnEnableDisable_Discoverable = findViewById<Button>(R.id.btnDiscoverable_on_off)
        lvNewDevices = findViewById<ListView>(R.id.lvNewDevices)
        mBTDevices = ArrayList()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btnONOFF.setOnClickListener {
            println( "onClick: enabling/disabling bluetooth.")
            enableDisableBT()
        }
    }


    fun enableDisableBT() {
        if (mBluetoothAdapter == null) {
            println( "enableDisableBT: Does not have BT capabilities.")
        }
        if (!mBluetoothAdapter!!.isEnabled) {
            println( "enableDisableBT: enabling BT.")
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBTIntent)
            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(mBroadcastReceiver1, BTIntent)
        }
        if (mBluetoothAdapter!!.isEnabled) {
            println( "enableDisableBT: disabling BT.")
            mBluetoothAdapter!!.disable()
            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(mBroadcastReceiver1, BTIntent)
        }
    }


    fun btnEnableDisable_Discoverable(view: View?) {
        println(
            "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds."
        )
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(mBroadcastReceiver2, intentFilter)
    }

    fun btnDiscover(view: View?) {
        println( "btnDiscover: Looking for unpaired devices.")
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
            println( "btnDiscover: Canceling discovery.")

            //check BT permissions in manifest
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent)
        }
        if (!mBluetoothAdapter!!.isDiscovering) {

            //check BT permissions in manifest
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent)
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private fun checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            var permissionCheck =
                checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
            if (permissionCheck != 0) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1001
                ) //Any number
            }
        } else {
            println(
                "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP."
            )
        }
    }
}