package com.fjk.camera.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = NetCameraDbConfig.TABLE_NAME)
class NetCamera(var name: String, var url: String,val singId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    override fun toString(): String {
        return "netCamera: name: $name   streamAddress: $url  singId: $singId"
    }
}