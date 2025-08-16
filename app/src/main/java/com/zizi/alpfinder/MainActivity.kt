package com.zizi.alpfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zizi.alpfinder.ui.scan.BarcodeScannerScreen
import com.zizi.alpfinder.ui.scan.BarcodeViewModel
import com.zizi.alpfinder.ui.theme.AlpfinderTheme

class MainActivity : ComponentActivity() {
    private val barcodeViewModel: BarcodeViewModel by viewModels { BarcodeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlpfinderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BarcodeScannerScreen(
                        modifier = Modifier.padding(innerPadding),
                        barcodeViewModel = barcodeViewModel,
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlpfinderTheme {
        Greeting("Android")
    }
}
