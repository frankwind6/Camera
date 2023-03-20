package com.fjk.camera

import android.os.Environment
import android.widget.Toast
import java.io.File

/**
 * @auth: njb
 * @date: 2021/10/20 17:47
 * @desc: 文件工具类
 */
object FileUtils {
    /**
     * 获取视频文件路径
     */
    fun getVideoName(): String {
        val videoPath = Environment.getExternalStorageDirectory().toString() + "/CameraX"
        val dir = File(videoPath)
        if (!dir.exists() && !dir.mkdirs()) {
//            ToastUtils.shortToast("文件不存在")
        }
        return videoPath
    }

    /**
     * 获取图片文件路径
     */
    fun getImageFileName(): String {
        val imagePath = Environment.getExternalStorageDirectory().toString() + "/images"
        val dir = File(imagePath)
        if (!dir.exists() && !dir.mkdirs()) {
//            Toast.makeText(contex,"文件不存在 ", Toast.LENGTH_LONG).show()
        }
        return imagePath
    }
}