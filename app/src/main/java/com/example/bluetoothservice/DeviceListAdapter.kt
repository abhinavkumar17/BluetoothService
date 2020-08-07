package com.example.bluetoothservice

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class DeviceListAdapter(
    context: Context,
    tvResourceId: Int,
    private val mDevices: ArrayList<BluetoothDevice>
) :
    ArrayAdapter<BluetoothDevice?>(context, tvResourceId, mDevices as List<BluetoothDevice?>) {

    private val mLayoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mViewResourceId: Int = tvResourceId


    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup): View {

        var convertView = convertView
        convertView = mLayoutInflater.inflate(mViewResourceId, null)
        val device = mDevices[position]
        if (device != null) {
            val deviceName =
                convertView.findViewById<View>(R.id.tvDeviceName) as TextView
            val deviceAdress =
                convertView.findViewById<View>(R.id.tvDeviceAddress) as TextView
            if (deviceName != null) {
                deviceName.text = device.name
            }
            if (deviceAdress != null) {
                deviceAdress.text = device.address
            }
        }
        return convertView
    }

}