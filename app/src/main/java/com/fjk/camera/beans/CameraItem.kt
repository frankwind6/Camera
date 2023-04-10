package com.fjk.camera.beans

import com.fjk.camera.adpter.CameraType

data class CameraItem(
    @CameraType.CameraType val type: Int,
    var name: String,
    val usbId: String,
    var netUrl: String,
    var isSelected: Boolean,
    val singId:Long
)

