package com.example.tugaslagi.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class SensorDataManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 1. Kategori Environment: Ambient Light Sensor
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // 2. Kategori Position: Proximity Sensor
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    // 3. Kategori Motion (Bukan Accelerometer): Gyroscope Sensor
    private val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lightState = MutableStateFlow(
        LightSensorState(
            isAvailable = lightSensor != null,
            sensorName = lightSensor?.name ?: "Sensor Cahaya Tidak Tersedia",
            vendor = lightSensor?.vendor ?: "N/A",
            maxRange = lightSensor?.maximumRange ?: 10000f
        )
    )
    val lightState: StateFlow<LightSensorState> = _lightState.asStateFlow()

    private val _proximityState = MutableStateFlow(
        ProximitySensorState(
            isAvailable = proximitySensor != null,
            sensorName = proximitySensor?.name ?: "Sensor Proximity Tidak Tersedia",
            vendor = proximitySensor?.vendor ?: "N/A",
            maxRange = proximitySensor?.maximumRange ?: 5f
        )
    )
    val proximityState: StateFlow<ProximitySensorState> = _proximityState.asStateFlow()

    private val _gyroscopeState = MutableStateFlow(
        GyroscopeSensorState(
            isAvailable = gyroscopeSensor != null,
            sensorName = gyroscopeSensor?.name ?: "Sensor Gyroscope Tidak Tersedia",
            vendor = gyroscopeSensor?.vendor ?: "N/A"
        )
    )
    val gyroscopeState: StateFlow<GyroscopeSensorState> = _gyroscopeState.asStateFlow()

    // Vibrator untuk efek haptic saat Proximity terpicu (NEAR)
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var lastGyroTimestamp: Long = 0L
    private var currentRotX = 0f
    private var currentRotY = 0f
    private var currentRotZ = 0f

    /**
     * Wajib dipanggil saat Activity onResume()
     * Mendaftarkan listener sensor untuk menghemat daya saat aktif saja.
     */
    fun startListening() {
        if (_isListening.value) return

        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        lastGyroTimestamp = 0L
        _isListening.value = true
    }

    /**
     * Wajib dipanggil saat Activity onPause()
     * Melepaskan listener sensor untuk mencegah battery drain dan memory leak.
     */
    fun stopListening() {
        if (!_isListening.value) return

        sensorManager.unregisterListener(this)
        _isListening.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                _lightState.value = _lightState.value.copy(
                    lux = lux
                )
            }

            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 5f
                // Pada banyak sensor Android, distance < maxRange menandakan objek DEKAT (NEAR)
                val isNear = distance < maxRange && distance < 4f

                val previousNear = _proximityState.value.isNear
                _proximityState.value = _proximityState.value.copy(
                    distance = distance,
                    isNear = isNear
                )

                // Efek getaran haptic saat objek mendekat pertama kali
                if (isNear && !previousNear) {
                    triggerHapticFeedback()
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                if (lastGyroTimestamp != 0L) {
                    val dt = (event.timestamp - lastGyroTimestamp) * 1.0e-9f // konversi nanodetik ke detik
                    val radToDeg = (180.0 / Math.PI).toFloat()

                    // Akumulasi rotasi sudut dengan damping
                    currentRotX = (currentRotX + x * dt * radToDeg).coerceIn(-45f, 45f) * 0.98f
                    currentRotY = (currentRotY + y * dt * radToDeg).coerceIn(-45f, 45f) * 0.98f
                    currentRotZ = (currentRotZ + z * dt * radToDeg).coerceIn(-180f, 180f) * 0.99f
                }
                lastGyroTimestamp = event.timestamp

                _gyroscopeState.value = _gyroscopeState.value.copy(
                    x = x,
                    y = y,
                    z = z,
                    rotationAngleX = currentRotX,
                    rotationAngleY = currentRotY,
                    rotationAngleZ = currentRotZ
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Penanganan perubahan akurasi sensor jika diperlukan
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (_: Exception) {
            // Abaikan jika perangkat tidak memiliki vibrator hardware
        }
    }

    // Fungsi utilitas untuk simulator / pengujian interaktif (misal pada emulator tanpa sensor fisik)
    fun simulateLight(lux: Float) {
        _lightState.value = _lightState.value.copy(lux = lux)
    }

    fun simulateProximity(isNear: Boolean) {
        _proximityState.value = _proximityState.value.copy(
            isNear = isNear,
            distance = if (isNear) 0f else 5f
        )
        if (isNear) triggerHapticFeedback()
    }

    fun simulateGyro(tiltX: Float, tiltY: Float, tiltZ: Float) {
        _gyroscopeState.value = _gyroscopeState.value.copy(
            rotationAngleX = tiltX,
            rotationAngleY = tiltY,
            rotationAngleZ = tiltZ,
            x = tiltX / 10f,
            y = tiltY / 10f,
            z = tiltZ / 10f
        )
    }
}
