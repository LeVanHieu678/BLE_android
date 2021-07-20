package com.vti.holderble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeDeviceListAdapter(var context: Context) :
    RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder>() {
    private var deviceScanners = mutableListOf<BluetoothDevice>()
    private var listener : OnDeviceClick? = null
    fun setListener(mListener: OnDeviceClick){
        listener = mListener
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val nameDevices: TextView = view.findViewById(R.id.tvNameDevice)
        fun bind(device: BluetoothDevice, position: Int) {
             nameDevices.text = device.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_device_scanner, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = deviceScanners[position]
        holder.bind(device,position)
        holder.itemView.setOnClickListener{
            listener?.onClickDeviceName(device)
        }
    }

    override fun getItemCount() = deviceScanners.size

    fun addDevice(device: BluetoothDevice) {
        if (!deviceScanners.contains(device)) {
            deviceScanners.add(device)
        }
    }
    interface OnDeviceClick{
        fun onClickDeviceName(bleDevice: BluetoothDevice)
    }

}
