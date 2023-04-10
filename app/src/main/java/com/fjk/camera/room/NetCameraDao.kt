package com.fjk.camera.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NetCameraDao {

    @Insert
    fun insertNetCamera(netCamera: NetCamera): Long

    @Update
    fun updateNetCamera(netCamera: NetCamera)

    @Query("select * from " + NetCameraDbConfig.TABLE_NAME)
    fun queryAllNetCamera(): List<NetCamera>

    @Delete
    fun deleteNetCamera(netCamera: NetCamera)

    @Query("delete from " + NetCameraDbConfig.TABLE_NAME + " where singId=:singId")
    fun deleteBySignId(singId: Long): Int

    @Query("select * from " + NetCameraDbConfig.TABLE_NAME + " where singId=:singId")
    fun queryBySignId(singId: Long): NetCamera
}