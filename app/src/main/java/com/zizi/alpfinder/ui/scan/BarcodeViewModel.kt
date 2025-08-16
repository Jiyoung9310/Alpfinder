package com.zizi.alpfinder.ui.scan

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

class BarcodeViewModel : ViewModel() {
    // 스캔된 바코드 값을 UI에 전달하기 위한 StateFlow
    private val _scannedBarcodeValue = MutableStateFlow<String?>(null)
    val scannedBarcodeValue = _scannedBarcodeValue.asStateFlow()

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // 카메라 UseCase를 바인딩하는 함수
    @androidx.camera.core.ExperimentalGetImage
    fun bindCameraUseCases(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview 설정
            val preview =
                Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // ImageAnalysis 설정
            val imageAnalyzer =
                ImageAnalysis
                    .Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image =
                                    InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees,
                                    )

                                val options =
                                    BarcodeScannerOptions
                                        .Builder()
                                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                        .build()
                                val scanner = BarcodeScanning.getClient(options)

                                scanner
                                    .process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty()) {
                                            // 인식 성공 시 StateFlow에 값 업데이트
                                            _scannedBarcodeValue.value = barcodes.first().rawValue
                                        }
                                    }.addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                        }
                    }

            // UseCase 바인딩
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer,
                )
            } catch (e: Exception) {
                // Log error
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // UI에서 결과 처리가 끝났을 때 호출하여 StateFlow를 초기화
    fun resetBarcodeValue() {
        _scannedBarcodeValue.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    BarcodeViewModel()
                }
            }
    }
}
