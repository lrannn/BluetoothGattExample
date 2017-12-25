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
 * @email liuran@yinkman.com
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
        textView.text = context.getString(R.string.service_group_uuid, mServices[groupPosition].characteristics[childPosition].uuid.toString())
        mPropertiesTV.text = context.getString(R.string.child_properties_text, propertiesText)
        return view
    }


    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_group_view, null)
        val textView = view.findViewById<TextView>(R.id.text_group_uuid)
        textView.text = context.getString(R.string.service_group_uuid, mServices[groupPosition].uuid.toString())
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
            mBuild.append("/")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
            mBuild.append("写")
            mBuild.append("/")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
            mBuild.append("无回应写")
            mBuild.append("/")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0) {
            mBuild.append("广播")
            mBuild.append("/")
        }
        if ((flag and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            mBuild.append("通知")
            mBuild.append("/")
        }
        return mBuild.toString()
    }

}