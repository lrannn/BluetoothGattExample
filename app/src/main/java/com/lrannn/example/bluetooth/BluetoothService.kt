package com.lrannn.example.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast

/**
 * Created by lrannn on 2017/12/20.
 * @email liuran@yinkman.com
 */
class BluetoothService : Service() {

    companion object {
        val ACTION_GATT_CONNECTED = "com.lrannn.example.bluetooth.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.lrannn.example.bluetooth.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "com.lrannn.example.bluetooth.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.lrannn.example.bluetooth.ACTION_DATA_AVAILABLE"
        val ACTION_RSSI_CHANGED = "com.lrannn.example.bluetooth.ACTION_RSSI_CHANGED"
        val EXTRA_DATA = "com.lrannn.example.bluetooth.EXTRA_DATA"
        val EXTRA_RSSI_DATA = "com.lrannn.example.bluetooth.EXTRA_RSSI_DATA"
    }

    private val mBinder = LocalBinder()
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mCurrentDevice: BluetoothDevice? = null


    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val intent = Intent(ACTION_RSSI_CHANGED)
                intent.putExtra(EXTRA_RSSI_DATA, rssi)
                updateBroadCast(intent)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                mBluetoothGatt?.discoverServices()
                mBluetoothGatt?.readRemoteRssi()
                updateBroadCast(ACTION_GATT_CONNECTED)
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                updateBroadCast(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateBroadCast(ACTION_DATA_AVAILABLE, characteristic!!)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateBroadCast(ACTION_GATT_SERVICES_DISCOVERED)
            }
        }
    }

    fun setupBluetoothAdapter(): Boolean {
        val mBluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not support on this device", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }


    fun connect(deviceAddr: String): Boolean {
        if (mBluetoothAdapter == null)
            return false

        try {
            val device = mBluetoothAdapter?.getRemoteDevice(deviceAddr)
            if (device != null) {
                connect(device)
                return true
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "连接失败~", Toast.LENGTH_SHORT).show()
        }
        return false

    }


    private fun connect(mDevice: BluetoothDevice): Boolean {
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback)
        if (mBluetoothGatt != null) {
            mCurrentDevice = mDevice
            return true
        } else Toast.makeText(this, "Connect failed！", Toast.LENGTH_SHORT).show()
        return false
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean? = mBluetoothGatt?.writeCharacteristic(characteristic)


    fun disconnect() {
        if (mBluetoothAdapter == null) {
            return
        }
        mBluetoothGatt?.disconnect()
    }

    fun isConnected(): Boolean = mBluetoothGatt?.getConnectionState(mCurrentDevice) == BluetoothGatt.STATE_CONNECTED

    fun close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt?.close()
            mBluetoothGatt = null
        }
    }

    fun getSupportGattServices(): List<BluetoothGattService>? {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt!!.services
        }
        return null
    }


    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }


    private fun updateBroadCast(action: String, characteristic: BluetoothGattCharacteristic? = null) {
        this.updateBroadCast(Intent(action), characteristic)
    }

    private fun updateBroadCast(intent: Intent, characteristic: BluetoothGattCharacteristic? = null) {
        if (characteristic != null) {
            val bytes = characteristic.value
            if (bytes != null && !bytes.isNotEmpty()) {
                val mBuild = StringBuilder(bytes.size)
                for (byte in bytes) {
                    mBuild.append(byte)
                }
                intent.putExtra(EXTRA_DATA, String(bytes) + "\n" + mBuild.toString())
            }
        }
        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {

        fun getService(): BluetoothService {
            return this@BluetoothService
        }

    }

}