package com.example.bluetoothservice

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    // Create a BroadcastReceiver for ACTION_FOUND
    private val mBluetootEnableBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> println("onReceive: STATE OFF")
                    BluetoothAdapter.STATE_TURNING_OFF -> println("mBroadcastReceiver1: STATE TURNING OFF")
                    BluetoothAdapter.STATE_ON -> println( "mBroadcastReceiver1: STATE ON")
                    BluetoothAdapter.STATE_TURNING_ON -> println("mBroadcastReceiver1: STATE TURNING ON")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnONOFF: Button = findViewById<Button>(R.id.btnONOFF)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btnONOFF.setOnClickListener {
            println( "onClick: enabling/disabling bluetooth.")
            enableDisableBT()
        }
    }

    fun enableDisableBT() {
        if (!mBluetoothAdapter.isEnabled) {
            println( "enableDisableBT: enabling BT.")
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBTIntent)
            val BtIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(mBluetootEnableBroadcastReceiver, BtIntent)
        }
        if (mBluetoothAdapter.isEnabled) {
            println( "enableDisableBT: disabling BT.")
            mBluetoothAdapter.disable()
            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(mBluetootEnableBroadcastReceiver, BTIntent)
        }
    }
}