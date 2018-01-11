/*
   Copyright 2017 lrannn

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.lrannn.example.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    companion object {
        private val REQUEST_BLE_ENABLE: Int = 0x00001
        private val TAG: String = "MainActivity"
    }

    private lateinit var mBleDeviceAdapter: BLEDeviceAdapter
    private val mHandler: Handler = Handler()
    private var mDeviceList: ArrayList<BluetoothDevice> = ArrayList()
    private var scaning: Boolean = false
    private var startScan: Boolean = false

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val mBLEDeviceCallback: BluetoothAdapter.LeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        addDevice(device)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkBluetoothFeatures()) {
            Toast.makeText(this, "BLE is not support!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
    }

    /**
     * Initilize all views
     */
    private fun initViews() {
        val mListView = findViewById<RecyclerView>(R.id.list_devices)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        mListView.layoutManager = manager
        mBleDeviceAdapter = BLEDeviceAdapter()
        mListView.adapter = mBleDeviceAdapter

        findViewById<FloatingActionButton>(R.id.fab_scan).setOnClickListener {
            if (scaning) {
                scanDevice(false)
            } else {
                mDeviceList.clear()
                scanDevice(true)
            }
            scaning = !scaning
        }
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


    /**
     * 检查是否有蓝牙功能
     */
    private fun checkBluetoothFeatures(): Boolean {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            return false
        }

        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        return mBluetoothAdapter != null
    }

    /**
     * 扫描设备
     * @param enable 开启or关闭
     */
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

    /**
     * 添加设备
     */
    private fun addDevice(device: BluetoothDevice) {
        runOnUiThread {
            if (!mDeviceList.contains(device))
                mDeviceList.add(device)

            mBleDeviceAdapter.notifyDataSetChanged()
        }
    }

    inner class BLEDeviceAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            return mDeviceList.size
        }

        private var context: Context? = null

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            context = parent?.context
            val view = LayoutInflater.from(context).inflate(R.layout.item_ble_devices, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val device = mDeviceList[position]
            if (device.name.isNullOrEmpty()) {
                holder?.mDeviceNameOnCenter?.text = context?.getString(R.string.ble_unknow_device_name)
                holder?.mDeviceNameOnCenter?.visibility = View.VISIBLE
                holder?.mDeviceAddr?.visibility = View.GONE
                holder?.mDeviceName?.visibility = View.GONE
            } else {
                holder?.mDeviceName?.text = device.name
                holder?.mDeviceAddr?.text = device.address
                holder?.mDeviceAddr?.visibility = View.VISIBLE
                holder?.mDeviceName?.visibility = View.VISIBLE
                holder?.mDeviceNameOnCenter?.visibility = View.GONE
            }
            holder?.mDeviceCard?.setOnClickListener({
                val intent = Intent(this@MainActivity, BLEGattMsgActivity::class.java)
                val uuid = device.uuids?.get(0)?.uuid.toString()
                intent.putExtra(BLEGattMsgActivity.EXTRA_DEVICE_NAME, device.name)
                intent.putExtra(BLEGattMsgActivity.EXTRA_DEVICE_ADDRESS, device.address)
                intent.putExtra(BLEGattMsgActivity.EXTRA_DEVICE_UUIDS, uuid)
                startActivity(intent)
            })
        }


    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val mDeviceName: TextView? = itemView?.findViewById(R.id.device_name)
        val mDeviceAddr: TextView? = itemView?.findViewById(R.id.device_addr)
        val mDeviceNameOnCenter: TextView? = itemView?.findViewById(R.id.device_name_center)
        val mDeviceCard: CardView? = itemView?.findViewById(R.id.device_cardview)
    }

}
