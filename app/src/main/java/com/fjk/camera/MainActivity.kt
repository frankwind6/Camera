package com.fjk.camera

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.System.DATE_FORMAT
import android.util.Log

import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.fjk.camera.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import org.jetbrains.annotations.NotNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    private var _mRunning:Boolean=false
    private var imageCamera: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture!!.get();

        binding.start.setOnClickListener {
            if (!_mRunning)
            bindPreview(cameraProvider)
        }

        binding.end.setOnClickListener{
            cameraProvider.unbindAll()
            _mRunning = false
        }

        binding.takePhoto.setOnClickListener{
            takePhoto()
        }
    }

    private fun bindPreview(@NotNull cameraProvider: ProcessCameraProvider) {

        val preview: Preview = Preview.Builder().build();
        imageCamera = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build();

        preview.setSurfaceProvider(binding.previewView.surfaceProvider);

        val camera: Camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview,imageCamera)
        _mRunning = true
    }

    @SuppressLint("SimpleDateFormat")
    private fun takePhoto() {
        val imageCapture = imageCamera ?: return
        val mFileForMat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val file = File(FileUtils.getImageFileName(), mFileForMat.format(Date()).toString() + ".jpg")
        Log.e("fjk", "Photo capture file: $file")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("fjk", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext,"拍照失败 + ${exc.message}",Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(file)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.e("fjk", msg)
                    Toast.makeText(baseContext,"拍照成功 + uri: $savedUri",Toast.LENGTH_LONG).show()
                }
            })
    }

}