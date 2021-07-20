@file:Suppress("DEPRECATION")

package com.vti.holderble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar


@Suppress("DEPRECATION")
class DeviceScanActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LeDeviceListAdapter.OnDeviceClick {
    private var googleApiClient: GoogleApiClient? = null

    //    private var adapter : LeDeviceListAdapter
    private var btnReScan: Button? = null
    private var scanning = false
    private var handler = Handler()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val SCAN_PERIOD: Long = 30000
    private var leDeviceListAdapter = LeDeviceListAdapter(this)
    private var rcvDeviceScan: RecyclerView? = null
    private var permissions = listOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.devive_scan_activity)
        rcvDeviceScan = findViewById(R.id.rcvDeviceScan)
        btnReScan = findViewById(R.id.btnScan)
        btnReScan?.setOnClickListener {
            reScanDeviceBLe()
        }
        rcvDeviceScan?.setHasFixedSize(true)
        rcvDeviceScan?.adapter = leDeviceListAdapter
        leDeviceListAdapter.setListener(this)
        val bluetoothMn = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothMn.adapter
        displayLocationSettings(this@DeviceScanActivity)
        requestPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        }
        scanDevice()
    }

    override fun onStart() {
        super.onStart()
        val intentFilterFoundDevice = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(scanReceiver, intentFilterFoundDevice)
    }

    private val scanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device!!.name.toString() + " - " + device.address
                    device.name?.let {
                        leDeviceListAdapter.addDevice(device)

                        leDeviceListAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun requestPermission() {

        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    this@DeviceScanActivity,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@DeviceScanActivity,
                    permissions.toTypedArray(),
                    1997
                )

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) run {
            displayLocationSettings(this@DeviceScanActivity)
        }
    }

    private fun displayLocationSettings(context: Context) {
        googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        googleApiClient?.connect()

    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanDevice() {
        if (!scanning) {
            handler.postDelayed(
                {
                    scanning = false
                    bluetoothAdapter?.stopLeScan(leScanCallBack)
                }, SCAN_PERIOD
            )
            scanning = true
            bluetoothAdapter?.startDiscovery()
        } else {
            scanning = false
            bluetoothAdapter?.stopLeScan(leScanCallBack)
        }
    }

    private fun reScanDeviceBLe() {
        bluetoothAdapter?.cancelDiscovery()
        bluetoothAdapter?.startDiscovery()
    }

    private val leScanCallBack: BluetoothAdapter.LeScanCallback =
        (BluetoothAdapter.LeScanCallback { device, _, _ ->

            device?.let { leDeviceListAdapter.addDevice(device = it) }
            leDeviceListAdapter.notifyDataSetChanged()
        }

//        override fun onScanResult(callbackType: Int, result: ScanResult?) {
//            super.onScanResult(callbackType, result)
//
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//            Toast.makeText(this@DeviceScanActivity, errorCode.toString(), Toast.LENGTH_SHORT).show()
//        }
//
//        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
//            super.onBatchScanResults(results)
//        }
                )

    override fun onConnected(p0: Bundle?) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (locationRequest.interval) / 2
        val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        settingsRequest.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(
                googleApiClient,
                settingsRequest.build()
            )
        result.setResultCallback {
            val status = it.status
            status.startResolutionForResult(this@DeviceScanActivity, 1997)
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onClickDeviceName(bleDevice: BluetoothDevice) {
        Snackbar.make(window.decorView, bleDevice.name, Snackbar.LENGTH_LONG).show()
        val deviceName = bleDevice.name
        val addressDevice = bleDevice.address
        val bundle = Bundle()
        bundle.putString(DeviceControllerActivity.EXTRA_ADDRESS, addressDevice)
        bundle.putString(DeviceControllerActivity.EXTRA_NAME, deviceName)
        val intent = Intent(this, DeviceControllerActivity::class.java)
        intent.putExtra(DeviceControllerActivity.DEVICE_BUNDLE, bundle)
        startActivity(intent)
//        Toast.makeText(this, bleDevice.name+ "=="+ bleDevice.address, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        unregisterReceiver(scanReceiver)
        super.onStop()

    }


}