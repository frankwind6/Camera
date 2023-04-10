package com.fjk.camera.adpter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.fjk.camera.R
import com.fjk.camera.beans.CameraItem


class CameraAdapter(private val owner: Context) :
    RecyclerView.Adapter<CameraAdapter.CmItemViewHolder>() {

    var cameraClickCallBack: ICameraClickCallBack? = null
    private lateinit var data: ArrayList<CameraItem>
    private var _lastClickTimeMills: Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CmItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_camera, parent, false)
        return CmItemViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: CmItemViewHolder, position: Int) {
        val item = data[position]
        holder.tvName.text = item.name
        holder.tvUseStatus.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE
        holder.ivRadio.isSelected = item.isSelected

        when (item.type) {
            CameraType.USB -> {
                holder.ivType.setImageResource(R.mipmap.usb)
                holder.btnEditCm.visibility = View.INVISIBLE
                holder.btnDeleteCm.visibility = View.INVISIBLE
            }
            else -> {
                holder.ivType.setImageResource(R.mipmap.net)
                holder.btnEditCm.visibility = View.VISIBLE
                holder.btnDeleteCm.visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - _lastClickTimeMills <= 1500 || item.isSelected) {
                return@setOnClickListener
            }
            _lastClickTimeMills = currentTime

            //显示loading的图标
            val loadingDrawable = owner.resources.getDrawable(R.mipmap.loading, null)
            if (loadingDrawable is AnimatedImageDrawable) {
                holder.ivRadio.setImageDrawable(loadingDrawable)
                loadingDrawable.start()
            }

            //显示loading，1秒之后再响应
            holder.ivRadio.postDelayed({
                selectItem(position)
                holder.ivRadio.setImageResource(R.drawable.select_camera)

                cameraClickCallBack?.click(item)
            }, 1000)
        }

        holder.btnDeleteCm.setOnClickListener {
            cameraClickCallBack?.btnDeleteClick(item)
        }

        holder.btnEditCm.setOnClickListener {
            cameraClickCallBack?.btnEditClick(item)
        }
    }


    override fun getItemCount(): Int = data.size

    inner class CmItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivRadio: AppCompatImageView = itemView.findViewById(R.id.img_radio)
        var tvName: AppCompatTextView = itemView.findViewById(R.id.tv_camera_name)
        var tvUseStatus: AppCompatTextView = itemView.findViewById(R.id.tv_camera_name_status)
        var ivType: AppCompatImageView = itemView.findViewById(R.id.tv_camera_type)
        var btnEditCm: Button = itemView.findViewById(R.id.btn_edit_cm)
        var btnDeleteCm: Button = itemView.findViewById(R.id.btn_delete_cm)
    }


    private fun selectItem(position: Int) {
        //检查传入的position是否有效
        require(position in 0 until itemCount) { "Invalid position: $position" }
        //用于查找最后一个选定的项目
        val lastPosition = data.indexOfFirst { it.isSelected }
        //仅更新已更改的项目的视图
        data.forEachIndexed { index, bean ->
            bean.isSelected = index == position
            if (bean.isSelected || index == lastPosition) {
                notifyItemChanged(index)
            }
        }
    }

    fun setData(data: ArrayList<CameraItem>) {
        this.data = data
    }

    fun getData(): ArrayList<CameraItem> {
        return this.data
    }
}

interface ICameraClickCallBack {
    fun click(item: CameraItem)
    fun btnEditClick(item: CameraItem)
    fun btnDeleteClick(item: CameraItem)
}