package com.zizi.alpfinder.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerScreen(
    modifier: Modifier = Modifier,
    barcodeViewModel: BarcodeViewModel,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    // 카메라 권한 요청을 위한 런처
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasCameraPermission = isGranted
            },
        )

    // Composable이 처음 그려질 때 권한 요청
    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ViewModel에서 스캔된 바코드 값을 관찰
    val scannedBarcode by barcodeViewModel.scannedBarcodeValue.collectAsState()

    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let {
            // TODO: 인식된 바코드 값(it)으로 API 호출 등 다음 작업 수행
            Toast.makeText(context, "인식된 바코드: $it", Toast.LENGTH_SHORT).show()

            // 한 번 처리 후에는 ViewModel의 상태를 초기화하여 중복 처리를 방지
            barcodeViewModel.resetBarcodeValue()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // 권한이 있으면 카메라 미리보기 표시
            AndroidView(
                factory = { context ->
                    val previewView = androidx.camera.view.PreviewView(context)
                    // ViewModel의 함수를 호출하여 카메라 UseCase 바인딩
                    barcodeViewModel.bindCameraUseCases(context, lifecycleOwner, previewView)
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // 권한이 없으면 안내 문구 표시
            Text(
                text = "카메라 권한이 필요합니다.",
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
