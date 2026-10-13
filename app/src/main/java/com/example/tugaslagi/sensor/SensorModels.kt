package com.example.tugaslagi.sensor

enum class SensorCategory(val displayName: String, val description: String) {
    ENVIRONMENT(
        displayName = "Environment (Lingkungan)",
        description = "Mengukur parameter fisik lingkungan sekitar perangkat (misal: intensitas cahaya, suhu, kelembaban)."
    ),
    POSITION(
        displayName = "Position (Posisi)",
        description = "Mengukur posisi fisik dan orientasi perangkat di ruang nyata (misal: jarak objek dekat/jauh via Proximity)."
    ),
    MOTION(
        displayName = "Motion (Gerakan)",
        description = "Mengukur gaya akselerasi dan rotasi sepanjang tiga sumbu (misal: kecepatan sudut via Gyroscope)."
    )
}

enum class LightAmbientLevel(val title: String, val description: String) {
    DARK("Gelap Gulita", "Intensitas cahaya sangat rendah (< 15 lux). Mode Malam aktif."),
    DIM("Redup / Ruang Tidur", "Pencahayaan temaram hangat (15 - 100 lux)."),
    NORMAL("Normal / Ruang Kerja", "Pencahayaan ruangan ideal (100 - 500 lux)."),
    BRIGHT("Terang Benderang", "Pencahayaan terang dalam ruangan atau dekat jendela (500 - 2000 lux)."),
    OUTDOOR_SUN("Cahaya Matahari Luar", "Intensitas cahaya tinggi luar ruangan (> 2000 lux).")
}

data class LightSensorState(
    val lux: Float = 0f,
    val maxRange: Float = 10000f,
    val isAvailable: Boolean = true,
    val sensorName: String = "Ambient Light Sensor",
    val vendor: String = "Android System"
) {
    val ambientLevel: LightAmbientLevel
        get() = when {
            lux < 15f -> LightAmbientLevel.DARK
            lux < 100f -> LightAmbientLevel.DIM
            lux < 500f -> LightAmbientLevel.NORMAL
            lux < 2000f -> LightAmbientLevel.BRIGHT
            else -> LightAmbientLevel.OUTDOOR_SUN
        }
}

data class ProximitySensorState(
    val distance: Float = 5f,
    val maxRange: Float = 5f,
    val isNear: Boolean = false,
    val isAvailable: Boolean = true,
    val sensorName: String = "Proximity Sensor",
    val vendor: String = "Android System"
)

data class GyroscopeSensorState(
    val x: Float = 0f, // rad/s
    val y: Float = 0f,
    val z: Float = 0f,
    val rotationAngleX: Float = 0f, // degrees for UI tilt
    val rotationAngleY: Float = 0f,
    val rotationAngleZ: Float = 0f,
    val isAvailable: Boolean = true,
    val sensorName: String = "Gyroscope Sensor",
    val vendor: String = "Android System"
)
