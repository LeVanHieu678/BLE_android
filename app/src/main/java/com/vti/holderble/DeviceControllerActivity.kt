package com.vti.holderble

import android.app.Service
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vti.holderble.service.BluetoothLeService

class DeviceControllerActivity : AppCompatActivity() {
    private var connected = false
    private var bluetoothService: BluetoothLeService? = null
    private var deviceAddress = String()
    private var deviceName = String()
    private var tvDeviceName: TextView? = null
    private var tvDeviceAddress:TextView? = null
    private var tvStatus:TextView? = null



    companion object {
        const val EXTRA_NAME = "EXTRA_NAME"
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        const val DEVICE_BUNDLE = "DEVICE_BUNDLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gatt_services_characteristics)
        tvDeviceAddress = findViewById(R.id.tv_addressDevice)
        tvDeviceName = findViewById(R.id.tv_nameDevice)
        tvStatus = findViewById(R.id.tv_status)
        val bundle = intent.extras?.getBundle(DEVICE_BUNDLE)
        deviceAddress = bundle?.get(EXTRA_ADDRESS) as String
        deviceName = bundle.get(EXTRA_NAME) as String
        tvDeviceName?.text = deviceName
        tvDeviceAddress?.text = deviceAddress
        val gattServerIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServerIntent, serviceConnection, Service.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdaterReceiver, makeGattUpdateIntentFilter())
    }


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Toast.makeText(
                        this@DeviceControllerActivity,
                        "Unable to initialize Bluetooth",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
               bluetooth.connect(address = deviceAddress)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
        }

    }
    private val gattUpdaterReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
                    updateConnectionState(R.string.connected)
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    updateConnectionState(R.string.disconnected)
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        displayGattService(bluetoothService?.getSupportedGattServices())
                    }
                }
            }
        }
    }

    private fun updateConnectionState(connected: Int) {
        tvStatus?.text = getString(connected)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun displayGattService(gattServices: MutableList<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid :String?
        val unknownServices: String = resources.getString(R.string.unknown_service)
        val unknownCharaString : String = resources.getString(R.string.unknown_characteristic)
        val gattCharacteristicData : MutableList<ArrayList<HashMap<String,String>>> = mutableListOf()
        val gattServiceData : MutableList<HashMap<String,String>> = mutableListOf()

        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String,String>()
            uuid = gattService.uuid.toString()
            currentServiceData[] =
        }




    }


    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)

        }
    }

    override fun onPause() {
        unregisterReceiver(gattUpdaterReceiver)
        super.onPause()
    }


}
