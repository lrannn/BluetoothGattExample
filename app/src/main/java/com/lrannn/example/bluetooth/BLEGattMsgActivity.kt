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

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog

/**
 * Created by lrannn on 2017/12/22.
 * @email liuran@yinkman.com
 */
class BLEGattMsgActivity : AppCompatActivity(), ExpandableListView.OnChildClickListener {
    companion object {
        val EXTRA_DEVICE_UUIDS: String = "com.lrannn.example.bluetooth.EXTRA_DEVICE_UUIDS"
        val EXTRA_DEVICE_ADDRESS: String = "com.lrannn.example.bluetooth.EXTRA_DEVICE_ADDRESS"
        val EXTRA_DEVICE_NAME: String = "com.lrannn.example.bluetooth.EXTRA_DEVICE_NAME"
    }

    private lateinit var mServiceName: String
    private lateinit var mServiceUUID: String
    private lateinit var mServiceAddr: String
    private var mService: BluetoothService? = null
    private val mHandler = Handler()

    private lateinit var mTextRssi: TextView
    private lateinit var mListView: ExpandableListView
    private var dialog: MaterialDialog? = null

    private val mServiceConnectionCallback: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null) {
                val binder = service as BluetoothService.LocalBinder
                mService = binder.getService()
                val success: Boolean = mService?.setupBluetoothAdapter()!!
                if (!success) {
                    Toast.makeText(this@BLEGattMsgActivity, "Service setup bluetooth failed!", Toast.LENGTH_LONG).show()
                } else {
                    val success = mService?.connect(mServiceAddr)
                    if (success!!) mHandler.removeCallbacks(null)
                }
            }
        }

    }
    private val mGattServiceReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null)
                return
            when (intent.action) {
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    showProgress(false)
                }
                BluetoothService.ACTION_GATT_DISCONNECTED -> {

                }
                BluetoothService.ACTION_DATA_AVAILABLE -> {

                }
                BluetoothService.ACTION_RSSI_CHANGED -> {
                    val rssi = intent.getIntExtra(BluetoothService.EXTRA_RSSI_DATA, 0)
                    mTextRssi.text = getString(R.string.device_info_rssi, rssi)
                }
                BluetoothService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    val gattServices = mService?.getSupportGattServices()
                    displayGattService(gattServices)
                }
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mServiceName = intent.getStringExtra(EXTRA_DEVICE_NAME)
        mServiceUUID = intent.getStringExtra(EXTRA_DEVICE_UUIDS)
        if (TextUtils.isEmpty(mServiceName)) mServiceName = "Unknow device"
        mServiceAddr = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
        setContentView(R.layout.activity_gatt_msg)

        showProgress(true)

        supportActionBar?.title = mServiceName

        (findViewById<TextView>(R.id.text_addr)).text = getString(R.string.device_info_address, mServiceAddr)
        mTextRssi = findViewById(R.id.text_rssi)
        mListView = findViewById(R.id.service_ex_listview)
        mListView.setOnChildClickListener(this)

        val serviceIntent = Intent(this, BluetoothService::class.java)
        bindService(serviceIntent, mServiceConnectionCallback, BIND_AUTO_CREATE)
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(mGattServiceReceiver, makeGattUpdateIntentFilter())
    }

    override fun onPause() {
        unregisterReceiver(mGattServiceReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        unbindService(mServiceConnectionCallback)
        super.onDestroy()
    }

    private fun showProgress(enable: Boolean) {
        if (dialog == null) {
            dialog = MaterialDialog.Builder(this)
                    .title("CONNECTING")
                    .content("Please wait")
                    .cancelable(false)
                    .progress(true, 0)
                    .show()
        }
        if (enable) {
            mHandler.postDelayed({
                if (dialog?.isShowing!!) {
                    dialog?.cancel()
                    finish()
                }
            }, 6 * 1000)
            dialog?.show()
        } else dialog?.cancel()

    }


    private fun displayGattService(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return

        gattServices.forEach {
            val adapter = ServiceEXListAdapter(gattServices, applicationContext)
            mListView.setAdapter(adapter)
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothService.ACTION_RSSI_CHANGED)
        return intentFilter
    }

    override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
        val gattServices = mService?.getSupportGattServices()
        if (gattServices != null) {
            val service = gattServices[groupPosition]
            val characteristic = service.characteristics[childPosition]
            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                showEditTextDialog(characteristic)
            }
        }
        return true
    }


    private var mInputDialog: MaterialDialog? = null

    private fun showEditTextDialog(characteristic: BluetoothGattCharacteristic) {
        if (mInputDialog == null) {
            mInputDialog = MaterialDialog.Builder(this)
                    .title("提示")
                    .input("输入字符", null) { dialog, input ->
                        val content = input.toString()
                        if (!content.isEmpty()) {
                            characteristic.setValue(content)
                            val successful = mService?.writeCharacteristic(characteristic)
                            if (successful!!) {
                                Toast.makeText(this, "发送成功，牛逼！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .build()
        }
        mInputDialog?.show()

    }

    private fun Context.log(tag: String = "lrannn", content: String) {
        Log.d(tag, content)
    }

}