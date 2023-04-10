package com.fjk.camera.wm

import android.content.Context
import cn.nodemedia.NodePlayer
import cn.nodemedia.NodePlayerView
import com.fjk.camera.adpter.CameraType
import com.fjk.camera.beans.CameraItem
import com.fjk.camera.databinding.ActivityMainBinding
import com.fjk.camera.room.NetCamera
import com.fjk.camera.room.NetCameraDao
import java.util.ArrayList

class NetCameraTaskMaster(
    binding: ActivityMainBinding,
    private val context: Context,
    private val dao: NetCameraDao
) {
    private var _nodePlayerView: NodePlayerView
    private var _nodePlayer: NodePlayer? = null

    init {
        _nodePlayerView = binding.nodePlayerView
    }

    fun getNetCameraData(): ArrayList<CameraItem> {
        var res = ArrayList<CameraItem>()
        //添加NET摄像头
        val queryAllNetCamera: List<NetCamera> = dao.queryAllNetCamera()
        for (item in queryAllNetCamera) {
            res.add(CameraItem(CameraType.NET, item.name, "-1", item.url, false, item.singId))
        }
        return res
    }

    fun showNetCameraView(url: String) {
        //设置渲染器类型
        _nodePlayerView.renderType = NodePlayerView.RenderType.SURFACEVIEW
        //设置视频画面缩放模式
        _nodePlayerView.setUIViewContentMode(NodePlayerView.UIViewContentMode.ScaleToFill)

        _nodePlayer = NodePlayer(context)
        _nodePlayer?.apply {
            //设置播放视图
            setPlayerView(_nodePlayerView)
            //设置RTSP流使用的传输协议,支持的模式有:
            setRtspTransport(NodePlayer.RTSP_TRANSPORT_TCP)
            setInputUrl(url)
            //设置视频是否开启
            setVideoEnable(true)
            //设置缓冲时间
            setBufferTime(100)
            //设置最大缓冲时间
            setMaxBufferTime(200)
            //设置连接等待超时
            setConnectWaitTimeout(10000)
            //
            setSubscribe(true)
            start()
        }
    }

    fun release() {
        _nodePlayer?.stop()
        _nodePlayer?.release()
    }
}