package com.fjk.camera.wm

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fjk.camera.adpter.CameraAdapter
import com.fjk.camera.adpter.CameraType
import com.fjk.camera.adpter.ICameraClickCallBack
import com.fjk.camera.beans.CameraItem
import com.fjk.camera.databinding.ActivityMainBinding
import com.fjk.camera.dialog.DeleteDialog
import com.fjk.camera.R
import com.fjk.camera.dialog.InputTwoMsgDialog
import com.fjk.camera.main.MainActivity
import com.fjk.camera.room.NetCamera
import com.fjk.camera.room.NetCameraDao
import com.fjk.camera.utils.ToastWhite
import com.fjk.camera.utils.URLUtil
import java.util.*
import kotlin.math.min


/**
 * 摄像头列表管理类
 */
@SuppressLint("SetTextI18n")
class RvCameraListManager(
    private val binding: ActivityMainBinding,
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val dao: NetCameraDao
) :
    ICameraClickCallBack, UsbCameraTaskMaster.UsbCameraCallBack {
    private val _cameraAdapter: CameraAdapter = CameraAdapter(context)
    private var _currentCameraType: Int = CameraType.USB
    private val _usbCameraTaskMaster: UsbCameraTaskMaster =
        UsbCameraTaskMaster(binding, context, lifecycleOwner)
    private val _netCameraTaskMaster: NetCameraTaskMaster =
        NetCameraTaskMaster(binding, context, dao)
    private val itemHeight = context.resources.getDimensionPixelSize(R.dimen.s_dp_56)
    private val lineHeight = context.resources.getDimensionPixelSize(R.dimen.s_dp_1)

    init {
        val intData = intData()
        initAdapter(intData)
        initOpenFirstCamera(intData)
        initAddNetCamera()
        _usbCameraTaskMaster.setUsbCameraCallBack(this)
    }

    /**
     * 初始化摄像头数据（数量、类型、使用情况等）
     */
    private fun intData(): ArrayList<CameraItem> {
        val data = ArrayList<CameraItem>()
        val usbData = _usbCameraTaskMaster.getUsbData()
        val netCameraData = _netCameraTaskMaster.getNetCameraData()
        data.addAll(usbData)
        data.addAll(netCameraData)
        return data
    }

    private fun initAdapter(data: ArrayList<CameraItem>) {
        _cameraAdapter.setData(data)
        binding.rvCameraList.layoutManager = LinearLayoutManager(context)

        binding.rvCameraList.adapter = _cameraAdapter

        binding.rvCameraList
            .addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        _cameraAdapter.cameraClickCallBack = this

        updateRvLayoutHeight()
    }

    /**
     * 根据内容自适应列表高度
     */
    private fun updateRvLayoutHeight() {
        val lp = binding.rvCameraList.layoutParams
        val size = _cameraAdapter.getData().size
        lp.height = (itemHeight + lineHeight) * min(size, 4)
        binding.rvCameraList.layoutParams = lp
    }

    private fun initOpenFirstCamera(data: ArrayList<CameraItem>) {
        if (data.size == 0) {
            return
        }
        if (data[0].type == CameraType.USB) {
            _usbCameraTaskMaster.bindPreview(data[0].usbId)
            data[0].isSelected = true
        }
    }

    private fun initAddNetCamera() {
        binding.addNetCamera.setOnClickListener {
            InputTwoMsgDialog.Builder(context)
                .setOnConfirmListener(object : InputTwoMsgDialog.OnConfirmListener {
                    override fun onClick(dialog: InputTwoMsgDialog) {
                        val cmName = dialog.binding.edItem1Input.text.toString()
                        val netUrl = dialog.binding.edItem2Input.text.toString()

                        val cameraItem = createNetCameraItem(cmName, netUrl)
                        addCamera(cameraItem)

                        dialog.dismiss()
                    }
                }).onCreate().showDialog(binding.rvCameraList)
        }
    }

    private fun createNetCameraItem(cmName: String, netUrl: String): CameraItem {
        val name = if (cmName.isEmpty() || cmName == "") {
            context.resources.getString(R.string.new_net_camera)
        } else {
            cmName
        }
        return CameraItem(CameraType.NET, name, "-1", netUrl, false, SystemClock.elapsedRealtime())
    }

    /**
     * 添加网络摄像头
     */
    private fun addCamera(item: CameraItem) {
        dao.insertNetCamera(NetCamera(item.name, item.netUrl, item.singId))
        val data = _cameraAdapter.getData()
        data.add(item)
        _cameraAdapter.setData(data)
        _cameraAdapter.notifyItemInserted(data.size - 1)
        updateRvLayoutHeight()
        ToastWhite.toast(context, R.string.add_net_camera_success, R.mipmap.icon_success)
    }

    /**
     * 修改摄像头信息
     */
    private fun modifyCamera(item: CameraItem) {
        updateNetCamera(item)

        val itemArrayList = _cameraAdapter.getData()
        val index = getItemIndex(itemArrayList, item.singId)
        if (index != -1) {
            itemArrayList[index] = item
            _cameraAdapter.setData(itemArrayList)
            _cameraAdapter.notifyItemChanged(index)
        }
    }

    /**
     * 查找指定单元id在列表中的索引值
     */
    private fun getItemIndex(itemArrayList: ArrayList<CameraItem>, singId: Long): Int {
        return itemArrayList.indexOfFirst { it.singId == singId }
    }

    private fun updateNetCamera(item: CameraItem) {
        val targetNetCamera = dao.queryBySignId(item.singId)
        targetNetCamera.name = item.name
        targetNetCamera.url = item.netUrl
        dao.updateNetCamera(targetNetCamera)
    }

    /**
     * 删除网络摄像头
     */
    fun deleteCamera(item: CameraItem) {
        dao.deleteBySignId(item.singId)
        _cameraAdapter.notifyItemRemoved(_cameraAdapter.getData().indexOf(item))
        _cameraAdapter.getData().remove(item)
        updateRvLayoutHeight()
    }

    override fun click(item: CameraItem) {
        Log.d(MainActivity.TAG, "click: =====收到点击事件")
        _currentCameraType = when (item.type) {
            CameraType.NET -> {
                hideTakePhotoLayout()
                Log.e(MainActivity.TAG, "click: 显示网络摄像头")
                _usbCameraTaskMaster.release()
                binding.nodePlayerView.visibility = View.VISIBLE
                binding.previewView.visibility = View.INVISIBLE
                val validateRtspUrlFormat = URLUtil.validateRtspUrlFormat(item.netUrl)
//                val rtspCanConnect = URLUtil.isRtspStreamAvailable(item.netUrl)
                if (validateRtspUrlFormat) {
                    hideTipCmNotConnect()
                    _netCameraTaskMaster.showNetCameraView(item.netUrl)
                } else {
                    showTipCmNotConnect()
                }
                Log.i("fjk", "click: rtsp地址格式对否： $validateRtspUrlFormat  ")
                CameraType.NET
            }
            CameraType.USB -> {
                showTakePhotoLayout()
                Log.i(MainActivity.TAG, "click: 显示USB摄像头")
                hideTipCmNotConnect()
                _netCameraTaskMaster.release()
                binding.nodePlayerView.visibility = View.INVISIBLE
                binding.previewView.visibility = View.VISIBLE
                if (!_usbCameraTaskMaster._mRunning) {
                    _usbCameraTaskMaster.bindPreview(item.usbId)
                }
                CameraType.USB
            }
            else -> {
                Log.d("FJK", "click: ======无效点击事件")
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun notifyUsbCameraData() {
        _cameraAdapter.setData(intData())
        _cameraAdapter.notifyDataSetChanged()
        updateRvLayoutHeight()
    }

    /**
     * 编辑摄像头信息
     */
    override fun btnEditClick(item: CameraItem) {
        InputTwoMsgDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.edit_net_camera))
            .setItem1Input(item.name)
            .setItem2Input(item.netUrl)
            .setOnConfirmListener(object : InputTwoMsgDialog.OnConfirmListener {
                override fun onClick(dialog: InputTwoMsgDialog) {
                    //未填名称，则设置默认名称
                    val cmName =
                        dialog.binding.edItem1Input.text.toString().takeIf { it.isNotEmpty() }
                            ?: context.resources.getString(R.string.new_net_camera)
                    item.name = cmName
                    item.netUrl = dialog.binding.edItem2Input.text.toString()
                    modifyCamera(item)   //刷新数据
                    dialog.dismiss()
                }
            }).onCreate().showDialog(binding.rvCameraList)
    }

    /**
     * 响应删除按钮操作
     */
    override fun btnDeleteClick(item: CameraItem) {
        val onCreate = DeleteDialog.Builder(context)
            .setOnCancelListener(object : DeleteDialog.OnCancelListener {
                override fun onClick(dialog: DeleteDialog) {
                    dialog.dismiss()
                }
            }).setDeleteListener(object : DeleteDialog.OnDeleteListener {
                override fun onClick(dialog: DeleteDialog) {
                    dialog.dismiss()
                    deleteCamera(item)
                }
            }).onCreate()
        onCreate.showDialog(binding.rvCameraList)
    }

    private fun showTipCmNotConnect() {
        binding.layoutNoSignal.root.visibility = View.VISIBLE
    }

    private fun hideTipCmNotConnect() {
        binding.layoutNoSignal.root.visibility = View.GONE
    }

    private fun showTakePhotoLayout() {
        binding.layoutTakePhoto.root.visibility = View.VISIBLE
    }

    private fun hideTakePhotoLayout() {
        binding.layoutTakePhoto.root.visibility = View.GONE
    }

    /**
     * 资源释放
     */
    fun release() {
        _netCameraTaskMaster.release()
        _usbCameraTaskMaster.release()
    }

    override fun usbDisconnect() {
        showTipCmNotConnect()
        hideTakePhotoLayout()
    }

    override fun usbConnect() {
        hideTipCmNotConnect()
        showTakePhotoLayout()
    }

    private fun View.makeBlackCurtainEffect() {
        visibility = View.VISIBLE
        Log.d("fjk", "imgCapture: =======制造黑一下的效果")
        postDelayed({ visibility = View.GONE }, 150)
    }

    override fun imgCapture() {
        binding.blackCurtainView.makeBlackCurtainEffect()
    }
}