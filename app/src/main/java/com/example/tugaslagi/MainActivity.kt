package com.example.tugaslagi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.tugaslagi.sensor.SensorDataManager
import com.example.tugaslagi.ui.SensorAppScreen
import com.example.tugaslagi.ui.theme.TugasLagiTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorDataManager: SensorDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi pengelola sensor
        sensorDataManager = SensorDataManager(this)

        setContent {
            TugasLagiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SensorAppScreen(
                        sensorDataManager = sensorDataManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Wajib Mengatur Lifecycle Sensor:
     * Listener sensor didaftarkan (register) saat Activity masuk ke onResume()
     * agar sensor mulai aktif membaca data ketika aplikasi tampil di layar.
     */
    override fun onResume() {
        super.onResume()
        if (::sensorDataManager.isInitialized) {
            sensorDataManager.startListening()
        }
    }

    /**
     * Wajib Mengatur Lifecycle Sensor:
     * Listener sensor dilepaskan (unregister) saat Activity masuk ke onPause()
     * untuk mencegah kebocoran memori (memory leak) dan pemborosan daya baterai
     * saat pengguna berpindah aplikasi atau mematikan layar.
     */
    override fun onPause() {
        super.onPause()
        if (::sensorDataManager.isInitialized) {
            sensorDataManager.stopListening()
        }
    }
}