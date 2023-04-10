package com.fjk.camera.utils

import android.net.Uri
import android.util.Log

object URLUtil {



    /**
     * 检查rtsp格式是否正确
     */
    fun validateRtspUrlFormat(rtspUrl: String): Boolean {
        Log.d("fjk", "validateRtspUrlFormat: ====rtspUrl $rtspUrl")
        // 检查URL是否为空
        if (rtspUrl.isNullOrEmpty()) {
            return false
        }
        // 检查URL是否以"rtsp://"开头
        if (!rtspUrl.startsWith("rtsp://")) {
            return false
        }
//        // 检查URL是否包含用户名和密码（如果需要）
        val uri = Uri.parse(rtspUrl)
//        if (uri.userInfo == null && uri.port != -1) {
//            return false
//        }
        // 检查URL的路径是否为空
        if (uri.path.isNullOrEmpty()) {
            return false
        }
        // 检查URL的协议是否为RTSP
        if (uri.scheme != "rtsp") {
            return false
        }
        // 如果URL格式正确，则返回true
        return true
    }
}