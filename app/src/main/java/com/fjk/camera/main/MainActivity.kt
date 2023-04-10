package com.fjk.camera.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fjk.camera.databinding.ActivityMainBinding
import com.fjk.camera.room.AppDatabase
import com.fjk.camera.room.NetCameraDao
import com.fjk.camera.wm.RvCameraListManager


class MainActivity : AppCompatActivity(), Handler.Callback {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var mBroadcastReceiver: BroadcastReceiver? = null
    private lateinit var controller: RvCameraListManager

    //创建dao接口
    private lateinit var netCameraDao: NetCameraDao

    private var handler: Handler = Handler(Looper.getMainLooper(), this)

    companion object {
        const val TAG = "MainActivity"
        const val MSG_NOTIFY_DATA = 0X01
        const val DELAY_SEND_MSG = 500   // 单位ms
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerBroadcastReceiver()

        //数据库管理
        netCameraDao = AppDatabase.getDatabase(this).netCameraDao()

        controller = RvCameraListManager(binding, this, this, netCameraDao)

        binding.floatingActionButton.setOnClickListener {
            binding.cmListLayout.let {
                if (it.visibility == View.VISIBLE) {
                    it.visibility = View.INVISIBLE
                } else {
                    it.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 设置点击空白处，camera列表消失
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            //使用 Rect 对象来检查点击事件是否在特定区域内。这样可以减少不必要的计算，提高代码性能
            val clickRect = Rect(ev.x.toInt(), ev.y.toInt(), ev.x.toInt() + 1, ev.y.toInt() + 1)
            val viewRectOnScreen = Rect().apply {
                binding.cmListLayout.getGlobalVisibleRect(this)
            }
            val buttonRectOnScreen = Rect().apply {
                binding.floatingActionButton.getGlobalVisibleRect(this)
            }
            if (buttonRectOnScreen.contains(clickRect)) {  // 点击的悬浮按钮，不用处理
                return super.dispatchTouchEvent(ev)
            }
            if (!viewRectOnScreen.contains(clickRect)) {   // 点击了列表外的范围
                if (binding.cmListLayout.visibility == View.VISIBLE) {
                    binding.cmListLayout.visibility = View.INVISIBLE
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    //注册监听Usb设备插拔广播
    private fun registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, intent: Intent?) {
                    if (intent == null) {
                        return
                    }
                    //这里可以拿到USB设备对象
                    val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    when (intent.action) {
                        //插入USB设备
                        UsbManager.ACTION_USB_DEVICE_ATTACHED ->
                            if (usbDevice?.let { isUsbCameraDevice(it) } == true) {
                                Log.e("fjk", "onReceive: Usb摄像头已插入")
                                handler.sendEmptyMessageDelayed(
                                    MSG_NOTIFY_DATA,
                                    DELAY_SEND_MSG.toLong()
                                )
                            }
                        UsbManager.ACTION_USB_DEVICE_DETACHED ->
                            if (usbDevice?.let { isUsbCameraDevice(it) } == true) {
                                Log.e("fjk", "onReceive: Usb摄像头已拔出")
                                handler.sendEmptyMessageDelayed(
                                    MSG_NOTIFY_DATA,
                                    DELAY_SEND_MSG.toLong()
                                )
                            }
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(mBroadcastReceiver, intentFilter)
    }

    //反注册USB设备拔插监听
    private fun unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver)
        }
    }

    private val USB_CAMERA_TYPE: Int = 14; //可能跟不同系统设备相关，一般是某个固定值，可以打Log验证。

    /**
     * 判断当前Usb设备是否是Camera设备
     */
    private fun isUsbCameraDevice(usbDevice: UsbDevice): Boolean {
        Log.i(
            TAG,
            "isUsbCameraDevice  usbDevice " + usbDevice.productName + usbDevice.deviceClass + ", subclass = " + usbDevice.deviceSubclass
        )
        var isCamera = false;
        val interfaceCount: Int = usbDevice.interfaceCount
        for (i in 0 until interfaceCount) {
            val usbInterface: UsbInterface = usbDevice.getInterface(i)
            //usbInterface.getName()遇到过为null的情况
            if ((usbInterface.name == null || usbDevice.productName
                    .equals(usbInterface.name)) && usbInterface.interfaceClass == USB_CAMERA_TYPE
            ) {
                isCamera = true
                break
            }
        }
        Log.i(
            TAG,
            "onReceive usbDevice = " + usbDevice.productName + " isCamera = " + isCamera + " usbDevice.deviceId: " + usbDevice.deviceId
        )
        return isCamera

    }

    override fun onDestroy() {
        super.onDestroy()
        controller.release()
        unregisterBroadcastReceiver()
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_NOTIFY_DATA -> controller.notifyUsbCameraData()
        }
        return true
    }
}