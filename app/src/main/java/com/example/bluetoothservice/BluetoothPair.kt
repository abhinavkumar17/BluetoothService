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
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity


class BluetoothPair : AppCompatActivity() {


    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var btnEnableDisable_Discoverable: Button? = null

    var mBTDevices: ArrayList<BluetoothDevice> = ArrayList()

    var mDeviceListAdapter: DeviceListAdapter? = null

    var lvNewDevices: ListView? = null


    private val mEnableBTBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> println("onReceive: STATE OFF")
                    BluetoothAdapter.STATE_TURNING_OFF -> println(
                        "mBroadcastReceiver1: STATE TURNING OFF"
                    )
                    BluetoothAdapter.STATE_ON -> println( "mBroadcastReceiver1: STATE ON")
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
    private val mBTDiscoverableBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                val mode =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)
                when (mode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> println(
                        "mBroadcastReceiver2: Discoverability Enabled."
                    )
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE ->println(
                        "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections."
                    )
                    BluetoothAdapter.SCAN_MODE_NONE -> println(
                        "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections."
                    )
                    BluetoothAdapter.STATE_CONNECTING -> println(
                        "mBroadcastReceiver2: Connecting...."
                    )
                    BluetoothAdapter.STATE_CONNECTED -> println(
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
    private val mBTListAllDevicesBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            println( "onReceive: ACTION FOUND.")
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                mBTDevices.add(device)
                println("onReceive: " + device.name + ": " + device.address)
                mDeviceListAdapter =
                    context?.let { DeviceListAdapter(it, R.layout.device_adapter_view, mBTDevices) }
                lvNewDevices?.adapter = mDeviceListAdapter
            }
        }
    }

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private val mPairingBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //3 cases:
                //case1: bonded already
                if (mDevice.bondState == BluetoothDevice.BOND_BONDED) {
                    println( "BroadcastReceiver: BOND_BONDED.")
                }
                //case2: creating a bone
                if (mDevice.bondState == BluetoothDevice.BOND_BONDING) {
                    println( "BroadcastReceiver: BOND_BONDING.")
                }
                //case3: breaking a bond
                if (mDevice.bondState == BluetoothDevice.BOND_NONE) {
                    println( "BroadcastReceiver: BOND_NONE.")
                }
            }
        }
    }


    protected override fun onDestroy() {
        println( "onDestroy: called.")
        super.onDestroy()
        unregisterReceiver(mEnableBTBroadcastReceiver)
        unregisterReceiver(mBTDiscoverableBroadcastReceiver)
        unregisterReceiver(mBTListAllDevicesBroadcastReceiver)
        unregisterReceiver(mPairingBroadcastReceiver)
        //mBluetoothAdapter.cancelDiscovery();
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pair)
        val btnONOFF: Button = findViewById<Button>(R.id.btnONOFF)
        btnEnableDisable_Discoverable = findViewById<Button>(R.id.btnDiscoverable_on_off)
        lvNewDevices = findViewById<ListView>(R.id.lvNewDevices)
        mBTDevices = ArrayList()

        //Broadcasts when bond state changes (ie:pairing)
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mPairingBroadcastReceiver, filter)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        //lvNewDevices?.setOnItemClickListener(this@BluetoothPair)
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
            registerReceiver(mEnableBTBroadcastReceiver, BTIntent)
        }
        if (mBluetoothAdapter!!.isEnabled) {
            println( "enableDisableBT: disabling BT.")
            mBluetoothAdapter!!.disable()
            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(mEnableBTBroadcastReceiver, BTIntent)
        }
    }


    fun btnEnableDisable_Discoverable(view: View?) {
        println( "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(mBTDiscoverableBroadcastReceiver, intentFilter)
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
            registerReceiver(mBTListAllDevicesBroadcastReceiver, discoverDevicesIntent)
        }
        if (!mBluetoothAdapter!!.isDiscovering) {

            //check BT permissions in manifest
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(mBTListAllDevicesBroadcastReceiver, discoverDevicesIntent)
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
            var permissionCheck: Int =
                this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
            if (permissionCheck != 0) {
                this.requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1001
                ) //Any number
            }
        } else {
            println( "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.")
        }
    }

    fun onItemClick(
        adapterView: AdapterView<*>?,
        view: View?,
        i: Int,
        l: Long
    ) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter!!.cancelDiscovery()
        println( "onItemClick: You Clicked on a device.")
        val deviceName = mBTDevices[i].name
        val deviceAddress = mBTDevices[i].address
        println( "onItemClick: deviceName = $deviceName")
        println( "onItemClick: deviceAddress = $deviceAddress")

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            println( "Trying to pair with $deviceName")
            mBTDevices[i].createBond()
        }
    }

}