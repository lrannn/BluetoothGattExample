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
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

/**
 * Created by lrannn on 2017/12/22.
 * @email lran7master@gmail.com
 */
class ServiceEXListAdapter(data: List<BluetoothGattService>, mContext: Context) : BaseExpandableListAdapter() {

    private var mServices: List<BluetoothGattService> = data
    private val context = mContext

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_child_view, null)
        val textView = view.findViewById<TextView>(R.id.text_child_uuid)
        val mPropertiesTV = view.findViewById<TextView>(R.id.text_group_permissions)
        val properties = mServices[groupPosition].characteristics[childPosition].properties
        val propertiesText = checkProperties(properties)

        val childCharacteristics = mServices[groupPosition].characteristics[childPosition]
        val uuid = childCharacteristics.uuid.toString()
        var name = SampleGattAttributes.lookup(uuid)
        if (name != null) {
            val serialNum = uuid.substring(4, 8)
            textView.text = "$name : ${serialNum.toUpperCase()}"
        } else {
            textView.text = context.getString(R.string.service_group_uuid, uuid)
        }
        mPropertiesTV.text = context.getString(R.string.child_properties_text, propertiesText)
        return view
    }


    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_view, null)
        val textView = view.findViewById<TextView>(R.id.text_group_uuid)
        val uuid = mServices[groupPosition].uuid.toString()
        textView.text = (SampleGattAttributes.lookup(uuid) ?: context.getString(R.string.service_group_uuid, uuid.substring(4, 8)))
        return view
    }


    override fun getGroup(groupPosition: Int): Any {
        return mServices[groupPosition]
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mServices[groupPosition].characteristics.size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return mServices[groupPosition].characteristics[childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return mServices.size
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    private fun checkProperties(flag: Int): String {
        val mBuild = StringBuilder()

        if ((flag and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            mBuild.append("读")
            mBuild.append("|")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            mBuild.append("写")
            mBuild.append("|")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            mBuild.append("无回应写")
            mBuild.append("|")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0) {
            mBuild.append("广播")
            mBuild.append("|")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            mBuild.append("通知")
            mBuild.append("|")
        }
        mBuild.removeRange(mBuild.length - 2, mBuild.length - 1)
        return mBuild.toString()
    }

}