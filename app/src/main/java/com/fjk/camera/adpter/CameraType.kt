package com.fjk.camera.adpter

import androidx.annotation.IntDef

class CameraType {

    //注解：核心
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
    @MustBeDocumented
    @IntDef(USB, NET)
    annotation
    class CameraType

    @CameraType
    private var type: Int = 0

    fun setCameraType(@CameraType type: Int) {
        this.type = type
    }

    fun getCameraType(): String {
        if (USB == type) return "usb摄像头"
        return if (NET == type) "网络摄像头" else "未知"
    }

    companion object{
        const val USB = 0x2
        const val NET = 0x3
    }

}