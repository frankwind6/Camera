package com.fjk.camera.utils

import android.os.Environment
import android.util.Log
import java.io.File
import java.util.regex.Pattern

/**
 * @auth: njb
 * @date: 2021/10/20 17:47
 * @desc: 文件工具类
 */
object FileUtils {
    private val DCIM_PATH =
        "sdcard" + File.separator + Environment.DIRECTORY_DCIM + File.separator

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
     * 获取图片存储路径
     */
    fun getImageFileName(): String {
        val imagePath = DCIM_PATH
        val dir = File(imagePath)
        if (!dir.exists() && !dir.mkdirs()) {
//            Toast.makeText(contex,"文件不存在 ", Toast.LENGTH_LONG).show()
        }
        Log.d("TAG", "getImageFileName: =====imagePath: $imagePath")
        return imagePath
    }

    /**
     * @author zml2015
     * @Email zhengmingliang911@gmail.com
     * @Time 2017年4月24日 上午11:51:18
     * @Description
     *
     *判断是不是网址
     * @param url
     * @return
     * "rtsp://admin:admin123@192.168.121.229/stream/live?channel=0&type=0"
     */
    fun isWebUrl(url: String?): Boolean {
        val patternString = ("^((https|http|ftp|rtsp|mms)?://)"
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$")
        val p = Pattern
            .compile(patternString)
        val m = p.matcher(url)
        Log.e("FJK", m.matches().toString() + "---")
        return m.matches()
    }
}