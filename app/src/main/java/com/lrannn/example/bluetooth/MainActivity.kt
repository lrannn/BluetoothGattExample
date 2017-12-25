package com.lrannn.example.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        private val REQUEST_BLE_ENABLE: Int = 0x00001
    }

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val mHandler: Handler = Handler()
    private var startScan: Boolean = false
    private var mDeviceList: ArrayList<BluetoothDevice> = ArrayList()
    private lateinit var mBleDeviceAdapter: DevicesAdapter
    private var mService: BluetoothService? = null

    private lateinit var mContainer: FrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setIcon(R.drawable.ic_bluetooth_black_24dp)

        if (!checkBluetoothFeatures()) {
            Toast.makeText(this, "BLE is not support!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE is not support!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val mListView = findViewById<ListView>(R.id.list_devices)
        mListView.onItemClickListener = this
        mBleDeviceAdapter = DevicesAdapter()
        mListView.adapter = mBleDeviceAdapter

        findViewById<TextView>(R.id.btn_start).setOnClickListener({
            mDeviceList.clear()
            scanDevice(true)
        })

        findViewById<TextView>(R.id.btn_stop).setOnClickListener({
            scanDevice(false)
        })
    }


    override fun onResume() {
        super.onResume()
        if (!mBluetoothAdapter!!.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_BLE_ENABLE)
        }
        mDeviceList.clear()
        scanDevice(true)
    }

    override fun onPause() {
        scanDevice(false)
        super.onPause()
    }


    private fun scanDevice(enable: Boolean) {
        if (enable) {
            // Scan 5 seconds
            mHandler.postDelayed({
                scanDevice(false)
            }, 5000)
            startScan = true
            mBluetoothAdapter?.startLeScan(mBLEDeviceCallback)
        } else {
            startScan = false
            mBluetoothAdapter?.stopLeScan(mBLEDeviceCallback)
        }
    }

    private fun addDevice(device: BluetoothDevice) {
        runOnUiThread {
            if (!mDeviceList.contains(device))
                mDeviceList.add(device)
            mBleDeviceAdapter.notifyDataSetChanged()
        }
    }

    private fun checkBluetoothFeatures() = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

    private val mBLEDeviceCallback: BluetoothAdapter.LeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        addDevice(device)
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val device = mDeviceList[position]
        val intent = Intent(this@MainActivity, BLEGattMsgActivity::class.java)
        val uuid = device.uuids?.get(0)?.uuid.toString()
        intent.putExtra(BLEGattMsgActivity.EXTRA_DEVICE_NAME, device.name)
        intent.putExtra(BLEGattMsgActivity.EXTRA_DEVICE_ADDRESS, device.address)
        intent.putExtra(BLEGattMsgActivity.EXTRA_DEVICE_UUIDS, uuid)

        startActivity(intent)
    }

    inner class DevicesAdapter : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mHolder: ViewHolder?
            val mView: View?
            if (convertView == null) {
                mView = LayoutInflater.from(this@MainActivity).inflate(R.layout.item_ble_devices, null)
                mHolder = ViewHolder()
                mHolder.mDeviceName = mView?.findViewById(R.id.device_name)
                mView?.tag = mHolder
            } else {
                mView = convertView
                mHolder = convertView.tag as ViewHolder?
            }
            if (mDeviceList[position].name != null && mDeviceList[position].name.isNotEmpty())
                mHolder?.mDeviceName?.text = mDeviceList[position].name
            else
                mHolder?.mDeviceName?.text = getString(R.string.ble_unknow_device_name)
            return mView!!

        }

        override fun getItem(position: Int): Any = mDeviceList[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = mDeviceList.size


    }

    class ViewHolder {
        var mDeviceName: TextView? = null
    }


}