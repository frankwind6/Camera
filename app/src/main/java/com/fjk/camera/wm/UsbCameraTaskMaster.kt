package com.fjk.camera.wm

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.impl.CameraInfoInternal
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.util.Preconditions
import androidx.lifecycle.LifecycleOwner
import com.fjk.camera.adpter.CameraType
import com.fjk.camera.beans.CameraItem
import com.fjk.camera.databinding.ActivityMainBinding
import com.fjk.camera.utils.FileUtils
import com.google.common.util.concurrent.ListenableFuture
import org.jetbrains.annotations.NotNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.fjk.camera.R

class UsbCameraTaskMaster(
    private val binding: ActivityMainBinding,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    var _mRunning: Boolean = false
    private var _currentMode: String = MODE_PHOTO
    private var _currentUsbId: String? = null
    private var _currentCameraFilter: ExternalCameraFilter? = null
    private var _imageCapture: ImageCapture? = null
    private val _executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var _cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    private var _cameraProvider: ProcessCameraProvider
    private var _manager: CameraManager
    private var _usbCameraCallback: UsbCameraCallBack? = null


    companion object {
        private val TAG = this::class.simpleName
        const val MODE_PHOTO = "MODE_TAKE_PICTURE"
        const val MODE_RECODE = "MODE_START_RECODE"
    }

    init {
        _cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        _cameraProvider = _cameraProviderFuture!!.get()//获取相机信息
        _manager = (context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?)!!
        initCaptureLayout()//拍照功能

        binding.previewView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val width = binding.previewView.width
            val height = binding.previewView.height
            Log.d("Preview resolution", "Width: $width, Height: $height")
            binding.previewViewResolution.text = "Preview resolution Width: $width, Height: $height"
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initCaptureLayout() {
        initMode()

        binding.layoutTakePhoto.imgCapture.setOnClickListener {
            if (_currentMode == MODE_PHOTO) {
                Log.d("FJK", "initCapture: =====拍照点击")
                takePhoto()
            } else {
                Log.d("FJK", "initCapture: =====录像点击")
                takeVideo()
            }
        }

        binding.layoutTakePhoto.album.setOnClickListener {
            val intent = Intent()
            intent.component =
                ComponentName("com.h3c.filemanager", "com.h3c.filemanager.ui.ActivityMain")
            context.startActivity(intent)
        }

    }

    private fun initMode() {
        val modeSpinner = binding.layoutTakePhoto.modeSpinner
        // 设置选项列表
        val modes = listOf(
            context.getString(R.string.mode_take_photo),
            context.getString(R.string.mode_take_recode)
        )
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, modes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSpinner.adapter = adapter

        // 设置选项选择事件
        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        // 切换到拍照模式
                        _currentMode = MODE_PHOTO
                        binding.layoutTakePhoto.imgCapture.setImageResource(R.drawable.select_capture_image)
                    }
                    1 -> {
                        // 切换到录像模式
                        _currentMode = MODE_RECODE
                        binding.layoutTakePhoto.imgCapture.setImageResource(R.drawable.select_capture_recode)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 无操作
            }
        }
    }

    fun getUsbData(): ArrayList<CameraItem> {
        val res = ArrayList<CameraItem>()

        val mCameraIds: Array<String> = getCameraList()
        Log.d("fjk", "intData: =====mCameraIds.size: ${mCameraIds.size}")
        //过滤无效id
        for (id in mCameraIds) {
            try {
                val characteristics: CameraCharacteristics =
                    _manager.getCameraCharacteristics(id)
                val supportLevel =
                    characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                if (supportLevel == null || supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    Log.e("fjk", "Camera ID $id is not available.")
                    cameraIdIsUsing(id)
                } else {
                    Log.e("fjk", "Camera ID $id is available.")
                    res.add(CameraItem(CameraType.USB, "USB CAMERA $id", id, "", false, 0))
                }
            } catch (e: IllegalArgumentException) {
                cameraIdIsUsing(id)
                Log.e("fjk", "Camera ID $id is not available.")
                e.printStackTrace()
            }
        }
        return res
    }

    fun bindPreview() {
        bindPreview(_cameraProvider)
    }

    fun bindPreview(cameraId: String) {
        bindPreview(_cameraProvider, cameraId)
    }

    @SuppressLint("RestrictedApi")
    private fun bindPreview(provider: ProcessCameraProvider) {
        _currentUsbId?.let { configSet(provider, it) }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(@NotNull provider: ProcessCameraProvider, cameraId: String) {
        _currentUsbId = cameraId
        configSet(provider, cameraId)
    }

    @SuppressLint("RestrictedApi")
    private fun configSet(@NotNull provider: ProcessCameraProvider, cameraId: String) {
        _cameraProviderFuture?.addListener({
            _currentCameraFilter = ExternalCameraFilter(cameraId)
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .addCameraFilter(_currentCameraFilter!!)
                .build()

            // 获取相机的特性
            val characteristics = _manager.getCameraCharacteristics(cameraId)
            val maxResolution = getMaxResolution(characteristics)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(maxResolution)
                .build()

            //创建imageCapture对象
            _imageCapture = ImageCapture.Builder().setTargetResolution(maxResolution).build()

            val preview = Preview.Builder().setTargetResolution(maxResolution).build()
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
            try {
                provider.unbindAll()//先解绑所有用例
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                    _imageCapture,
                )//绑定用例
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
            _usbCameraCallback?.usbConnect()
            _mRunning = true
        }, ContextCompat.getMainExecutor(context))

    }

    private fun getMaxResolution(characteristics: CameraCharacteristics): Size {
        // 获取相机支持的输出流配置
        val configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        // 获取相机支持的所有输出分辨率
        val outputSizes = configs?.getOutputSizes(ImageFormat.JPEG)
        // 打印所有支持的分辨率信息
        if (outputSizes != null) {
            for (size in outputSizes) {
                Log.d("fjk", "支持的分辨率：${size.width} x ${size.height}")
            }
        }

        var maxResolution: Size? = null
        // 查找最大分辨率
        if (outputSizes != null) {
            for (size in outputSizes) {
                if (maxResolution == null || size.width * size.height > maxResolution.width * maxResolution.height) {
                    maxResolution = size
                }
            }
        }

        // 打印结果
        if (maxResolution != null) {
            Log.d("fjk", "支持的最大分辨率：${maxResolution.width} x ${maxResolution.height}")
            binding.supportResolution.text =
                "相机支持的最大分辨率：${maxResolution.width} x ${maxResolution.height}"
        }

        //如果为空，设置默认目标分辨率为1080P
        if (maxResolution == null) {
            maxResolution = Size(1920, 1080)
        }
        return maxResolution
    }

    private fun cameraIdIsUsing(id: String) {
        if (_currentUsbId == id) {
            closeUsbCamera()
            _currentUsbId = null
            _usbCameraCallback?.usbDisconnect()
        }
    }

    private fun closeUsbCamera() {
        _cameraProvider.unbindAll()
        _mRunning = false
    }

    /**
     * 拍照
     */
    @SuppressLint("SimpleDateFormat")
    fun takePhoto() {
        val imageCapture = _imageCapture ?: return
        val mFileForMat = SimpleDateFormat("yyyyMMdd_hhmmss")
        val file = File(
            FileUtils.getImageFileName(),
            "IMG_" + mFileForMat.format(Date()).toString() + ".jpg"
        )
        Log.e("fjk", "Photo capture file: $file")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("fjk", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(context, "拍照失败 + ${exc.message}", Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(file)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.e("fjk", msg)
                    Toast.makeText(context, "拍照成功 + uri: $savedUri", Toast.LENGTH_LONG).show()
                }
            })
        _usbCameraCallback?.imgCapture()
    }

    /**
     * 拍视频
     */
    @SuppressLint("MissingPermission", "RestrictedApi")
    private fun takeVideo() {
        val mFileForMat = SimpleDateFormat("yyyyMMdd_hhmmss")
        //视频保存路径
        val file = File(
            FileUtils.getImageFileName(),
            "IMG_" + mFileForMat.format(Date()).toString() + ".mp4"
        )
        val build: VideoCapture.OutputFileOptions =
            VideoCapture.OutputFileOptions.Builder(file).build()

    }

    /**
     * 定义摄像头过滤器
     */
    class ExternalCameraFilter(private val mId: String) : CameraFilter {
        @SuppressLint("RestrictedApi")
        override fun filter(cameraInfos: MutableList<CameraInfo>): MutableList<CameraInfo> {
            val result = ArrayList<CameraInfo>()
            cameraInfos.forEach {
                Preconditions.checkArgument(
                    it is CameraInfoInternal,
                    "The camera info doesn't contain internal implementation."
                )
                it as CameraInfoInternal
                val id = it.cameraId

                if (id == mId) {
                    result.add(it)
                }
            }
            return result
        }
    }

    private fun getCameraList(): Array<String> {
        return _manager.cameraIdList as Array<String>
    }

    fun release() {
        _cameraProvider.unbindAll()
        _mRunning = false
        _currentCameraFilter = null
        _executor.shutdown()
    }

    fun setUsbCameraCallBack(callBack: UsbCameraCallBack) {
        _usbCameraCallback = callBack
    }

    interface UsbCameraCallBack {
        fun usbDisconnect()
        fun usbConnect()
        fun imgCapture()
    }
}