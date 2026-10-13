package com.example.tugaslagi.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugaslagi.sensor.GyroscopeSensorState
import com.example.tugaslagi.sensor.LightAmbientLevel
import com.example.tugaslagi.sensor.LightSensorState
import com.example.tugaslagi.sensor.ProximitySensorState
import com.example.tugaslagi.sensor.SensorCategory
import com.example.tugaslagi.sensor.SensorDataManager
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorAppScreen(
    sensorDataManager: SensorDataManager,
    modifier: Modifier = Modifier
) {
    val isListening by sensorDataManager.isListening.collectAsState()
    val lightState by sensorDataManager.lightState.collectAsState()
    val proximityState by sensorDataManager.proximityState.collectAsState()
    val gyroState by sensorDataManager.gyroscopeState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dynamic Theme Color Calculation based on Light Sensor (Environment Category)
    val targetBgColor = when {
        lightState.lux < 15f -> Color(0xFF0B0F19) // Deep Midnight
        lightState.lux < 80f -> Color(0xFF1E1B4B) // Dark Indigo
        lightState.lux < 300f -> Color(0xFF1E293B) // Slate Cozy Room
        lightState.lux < 1000f -> Color(0xFF1E3A8A) // Daylight Blue
        else -> Color(0xFF0369A1) // Brilliant Sky
    }

    val targetAccentColor = when {
        proximityState.isNear -> Color(0xFFEF4444) // Warning Red when hand covers sensor
        lightState.lux < 80f -> Color(0xFF818CF8) // Soft Moon Glow
        lightState.lux < 500f -> Color(0xFFFBBF24) // Warm Amber Bulb
        else -> Color(0xFF38BDF8) // Bright Cyan Sky
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bgColorAnim"
    )

    val animatedAccentColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(durationMillis = 350),
        label = "accentColorAnim"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = animatedBgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header: Judul & Status Lifecycle Sensor
            HeaderSection(
                isListening = isListening,
                accentColor = animatedAccentColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Modern Segmented Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Studio Live", "Spesifikasi", "Simulator Uji").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) animatedAccentColor else Color.Transparent,
                        onClick = { selectedTab = index }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area based on selected Tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> InteractiveStudioTab(
                        lightState = lightState,
                        proximityState = proximityState,
                        gyroState = gyroState,
                        accentColor = animatedAccentColor
                    )
                    1 -> SensorSpecificationTab(
                        lightState = lightState,
                        proximityState = proximityState,
                        gyroState = gyroState
                    )
                    2 -> SimulatorTestingTab(
                        sensorDataManager = sensorDataManager,
                        lightState = lightState,
                        proximityState = proximityState,
                        gyroState = gyroState
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isListening: Boolean,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Android Sensor Explorer",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Eksplorasi Minimal 2 Sensor Lintas Kategori",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                // Lifecycle Indicator Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isListening) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFEF4444).copy(alpha = 0.25f),
                    border = BorderStroke(
                        1.dp,
                        if (isListening) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isListening) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isListening) "onResume (Aktif)" else "onPause (Nonaktif)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips Showcase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SensorCategoryBadge(
                    label = "1. Lingkungan (Light)",
                    activeColor = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
                SensorCategoryBadge(
                    label = "2. Posisi (Proximity)",
                    activeColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                SensorCategoryBadge(
                    label = "3. Gerakan (Gyro)",
                    activeColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SensorCategoryBadge(
    label: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = activeColor.copy(alpha = 0.15f),
        border = BorderStroke(0.8.dp, activeColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = activeColor,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

/**
 * TAB 1: STUDIO INTERAKTIF
 * Menampilkan efek visual langsung dari kedua sensor secara bersamaan:
 * - Kategori 1: Environment (Light Sensor) -> Mengubah tema pencahayaan, nyala lampu neon bohlam, dan lux meter.
 * - Kategori 2: Position (Proximity Sensor) -> Mengaktifkan Proximity Radar Shield & peringatan interaktif saat tangan mendekat.
 * - Kategori 3: Motion (Gyroscope Sensor) -> Memutar objek 3D disc / compass secara real-time.
 */
@Composable
private fun InteractiveStudioTab(
    lightState: LightSensorState,
    proximityState: ProximitySensorState,
    gyroState: GyroscopeSensorState,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. KARTU SENSOR LINGKUNGAN (ENVIRONMENT - LIGHT SENSOR)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Kategori: Environment (Lingkungan)",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ambient Light Sensor (Cahaya)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Badge level pencahayaan
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFBBF24).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = lightState.ambientLevel.title,
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Efek Langsung ke UI: Bohlam / Bola Cahaya Interaktif
                LightBulbInteractiveEffect(lux = lightState.lux)

                Spacer(modifier = Modifier.height(12.dp))

                // Meter Angka Lux
                Text(
                    text = "%.1f LUX".format(lightState.lux),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = lightState.ambientLevel.description,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar Intensitas Cahaya
                val normalizedLux = (lightState.lux / 1000f).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = normalizedLux,
                    animationSpec = tween(400),
                    label = "luxProgress"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0 Lux (Gelap)", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("500 Lux (Ruang Terang)", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("1000+ Lux (Luar)", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFBBF24),
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // 2. KARTU SENSOR POSISI (POSITION - PROXIMITY SENSOR)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Kategori: Position (Posisi)",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Proximity Sensor (Jarak Dekat)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Status NEAR / FAR
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (proximityState.isNear) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.25f),
                        border = BorderStroke(
                            1.dp,
                            if (proximityState.isNear) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            text = if (proximityState.isNear) "NEAR (DEKAT)" else "FAR (JAUH)",
                            color = if (proximityState.isNear) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Efek Langsung ke UI: Proximity Holographic Radar & Shield Animasi
                ProximityRadarEffect(
                    isNear = proximityState.isNear,
                    distance = proximityState.distance
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (proximityState.isNear)
                        "⚠️ PERISAI AKTIF! Objek/Tangan Terdeteksi Dekat Sensor"
                    else
                        "✓ Sensor Bebas (Tidak ada halangan di depan sensor)",
                    color = if (proximityState.isNear) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Jarak Terbaca: %.1f cm (Max Range: %.1f cm) • Disertai getaran haptic saat NEAR".format(
                        proximityState.distance,
                        proximityState.maxRange
                    ),
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 3. KARTU SENSOR GERAK (MOTION - GYROSCOPE SENSOR)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Kategori: Motion (Bukan Accelerometer)",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gyroscope Sensor (Kecepatan Rotasi)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFA78BFA).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "3-Axis Gyro",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Efek Langsung ke UI: Piringan 3D Gyroscope yang berputar mengikuti rotasi perangkat
                GyroDisc3DEffect(gyroState = gyroState)

                Spacer(modifier = Modifier.height(10.dp))

                // Sumbu X, Y, Z
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AxisMetricBadge(name = "X (Pitch)", value = gyroState.x, color = Color(0xFFEF4444))
                    AxisMetricBadge(name = "Y (Roll)", value = gyroState.y, color = Color(0xFF10B981))
                    AxisMetricBadge(name = "Z (Yaw)", value = gyroState.z, color = Color(0xFF3B82F6))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Putar atau miringkan smartphone untuk menggerakkan piringan kompas secara real-time.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AxisMetricBadge(name: String, value: Float, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(
                "%.2f rad/s".format(value),
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Visual Animasi Bohlam & Pendaran Cahaya (Environment: Light Sensor)
 */
@Composable
private fun LightBulbInteractiveEffect(lux: Float) {
    // Normalisasi lux ke faktor intensitas 0.15f s/d 1.0f
    val glowIntensity = (lux / 400f).coerceIn(0.12f, 1.0f)
    val glowRadiusFactor by animateFloatAsState(
        targetValue = glowIntensity,
        animationSpec = tween(400),
        label = "bulbGlow"
    )

    Box(
        modifier = Modifier
            .size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 4

            // Pendaran cahaya (glow halo) di luar bohlam
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD54F).copy(alpha = 0.85f * glowRadiusFactor),
                        Color(0xFFFFA000).copy(alpha = 0.45f * glowRadiusFactor),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius + (baseRadius * 1.5f * glowRadiusFactor)
                ),
                radius = baseRadius + (baseRadius * 1.5f * glowRadiusFactor),
                center = center
            )

            // Lingkaran inti bohlam
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = baseRadius,
                center = center
            )

            // Garis filamen bohlam
            drawCircle(
                color = Color(0xFFFF8F00),
                radius = baseRadius * 0.5f,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

/**
 * Visual Animasi Radar & Perisai Proximity (Position: Proximity Sensor)
 */
@Composable
private fun ProximityRadarEffect(
    isNear: Boolean,
    distance: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseProgress"
    )

    val radarColor = if (isNear) Color(0xFFEF4444) else Color(0xFF10B981)

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2

            // Gelombang radar memancar keluar saat NEAR
            if (isNear) {
                drawCircle(
                    color = radarColor.copy(alpha = (1f - pulseProgress).coerceIn(0f, 0.7f)),
                    radius = maxRadius * pulseProgress,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Lingkaran radar tengah
            drawCircle(
                color = radarColor.copy(alpha = 0.2f),
                radius = maxRadius * 0.75f,
                center = center
            )

            // Lingkaran inti
            drawCircle(
                color = radarColor,
                radius = if (isNear) 24.dp.toPx() else 18.dp.toPx(),
                center = center
            )

            // Tanda silang crosshair
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(center.x - 12.dp.toPx(), center.y),
                end = Offset(center.x + 12.dp.toPx(), center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - 12.dp.toPx()),
                end = Offset(center.x, center.y + 12.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Visual Piringan Gyroscope 3D (Motion: Gyroscope Sensor)
 */
@Composable
private fun GyroDisc3DEffect(gyroState: GyroscopeSensorState) {
    val rotX by animateFloatAsState(targetValue = gyroState.rotationAngleX, label = "gyroRotX")
    val rotY by animateFloatAsState(targetValue = gyroState.rotationAngleY, label = "gyroRotY")
    val rotZ by animateFloatAsState(targetValue = gyroState.rotationAngleZ, label = "gyroRotZ")

    Box(
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer {
                rotationX = rotX
                rotationY = rotY
                rotationZ = rotZ
                cameraDistance = 12 * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f

            // Piringan luar
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFA78BFA),
                        Color(0xFF38BDF8),
                        Color(0xFF34D399),
                        Color(0xFFA78BFA)
                    )
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Jarum kompas / arah
            val angleRad = Math.toRadians((rotZ - 90.0)).toFloat()
            val tipX = center.x + (radius * 0.7f) * cos(angleRad)
            val tipY = center.y + (radius * 0.7f) * sin(angleRad)

            drawLine(
                color = Color(0xFFEF4444),
                start = center,
                end = Offset(tipX, tipY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Titik tengah
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = center
            )
        }
    }
}

/**
 * TAB 2: SPESIFIKASI SENSOR
 * Menampilkan data teknis perangkat keras masing-masing sensor.
 */
@Composable
private fun SensorSpecificationTab(
    lightState: LightSensorState,
    proximityState: ProximitySensorState,
    gyroState: GyroscopeSensorState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Informasi Sensor & Kategori",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Penjelasan Kategori Wajib
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Kaidah Klasifikasi Sensor Android",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aplikasi ini memadukan sensor dari kategori berbeda:\n" +
                            "• Kategori Lingkungan (Environment): Sensor Cahaya (Ambient Light)\n" +
                            "• Kategori Posisi (Position): Sensor Proximity (Jarak Halangan)\n" +
                            "• Kategori Gerakan (Motion): Sensor Gyroscope (Rotasi Sudut 3-Axis)\n" +
                            "Keduanya tidak bergantung pada Accelerometer biasa.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Kartu Spesifikasi Sensor 1: Light
        SensorSpecCard(
            category = SensorCategory.ENVIRONMENT.displayName,
            sensorType = "android.hardware.Sensor.TYPE_LIGHT",
            name = lightState.sensorName,
            vendor = lightState.vendor,
            maxRange = "${lightState.maxRange} lux",
            currentReading = "%.2f lux".format(lightState.lux),
            isAvailable = lightState.isAvailable,
            accentColor = Color(0xFFFBBF24)
        )

        // Kartu Spesifikasi Sensor 2: Proximity
        SensorSpecCard(
            category = SensorCategory.POSITION.displayName,
            sensorType = "android.hardware.Sensor.TYPE_PROXIMITY",
            name = proximityState.sensorName,
            vendor = proximityState.vendor,
            maxRange = "${proximityState.maxRange} cm",
            currentReading = if (proximityState.isNear) "NEAR (%.1f cm)".format(proximityState.distance) else "FAR (%.1f cm)".format(proximityState.distance),
            isAvailable = proximityState.isAvailable,
            accentColor = Color(0xFF38BDF8)
        )

        // Kartu Spesifikasi Sensor 3: Gyroscope
        SensorSpecCard(
            category = SensorCategory.MOTION.displayName,
            sensorType = "android.hardware.Sensor.TYPE_GYROSCOPE",
            name = gyroState.sensorName,
            vendor = gyroState.vendor,
            maxRange = "Variatif (rad/s)",
            currentReading = "X: %.2f, Y: %.2f, Z: %.2f rad/s".format(gyroState.x, gyroState.y, gyroState.z),
            isAvailable = gyroState.isAvailable,
            accentColor = Color(0xFFA78BFA)
        )

        // Edukasi Lifecycle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.12f)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚙️ Manajemen Lifecycle Sensor",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• onResume(): Mendaftarkan SensorEventListener via sensorManager.registerListener().\n" +
                            "• onPause(): Melepaskan SensorEventListener via sensorManager.unregisterListener().\n" +
                            "Hal ini mencegah pemborosan baterai saat aplikasi beralih ke latar belakang.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SensorSpecCard(
    category: String,
    sensorType: String,
    name: String,
    vendor: String,
    maxRange: String,
    currentReading: String,
    isAvailable: Boolean,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAvailable) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isAvailable) "Hardware Tersedia" else "Hardware Tidak Ada",
                        color = if (isAvailable) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = sensorType,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            SpecRow(label = "Vendor", value = vendor)
            SpecRow(label = "Jangkauan Maksimum", value = maxRange)
            SpecRow(label = "Nilai Real-time Terkini", value = currentReading, highlightColor = accentColor)
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, highlightColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.5.sp)
        Text(
            text = value,
            color = highlightColor ?: Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * TAB 3: SIMULATOR / UJI MANUAL
 * Sangat berguna ketika didemokan pada Emulator Android atau ruangan dengan intensitas cahaya tetap.
 */
@Composable
private fun SimulatorTestingTab(
    sensorDataManager: SensorDataManager,
    lightState: LightSensorState,
    proximityState: ProximitySensorState,
    gyroState: GyroscopeSensorState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🧪 Panel Uji Interaktif (Simulator)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gunakan kontrol di bawah ini untuk mensimulasikan nilai sensor secara instan (misal saat running di Android Studio Emulator atau ruangan redup).",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // 1. Simulator Light Sensor
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Simulasi Sensor Cahaya (Environment)",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "%.0f Lux".format(lightState.lux),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = lightState.lux.coerceIn(0f, 1500f),
                    onValueChange = { sensorDataManager.simulateLight(it) },
                    valueRange = 0f..1500f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetButton("Malam (5 Lux)") { sensorDataManager.simulateLight(5f) }
                    PresetButton("Ruang (150 Lux)") { sensorDataManager.simulateLight(150f) }
                    PresetButton("Terang (800 Lux)") { sensorDataManager.simulateLight(800f) }
                }
            }
        }

        // 2. Simulator Proximity Sensor
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Simulasi Sensor Proximity (Position)",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (proximityState.isNear) "Status: Dekat (NEAR)" else "Status: Jauh (FAR)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = proximityState.isNear,
                        onCheckedChange = { sensorDataManager.simulateProximity(it) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Aktifkan sakelar di atas untuk menirukan tangan mendekati sensor dan memicu peringatan getaran haptic serta radar perisai.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        // 3. Simulator Gyroscope
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Simulasi Rotasi Gyroscope (Motion)",
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetButton("Miring Kiri (-30°)") { sensorDataManager.simulateGyro(0f, -30f, -45f) }
                    PresetButton("Tegak (0°)") { sensorDataManager.simulateGyro(0f, 0f, 0f) }
                    PresetButton("Miring Kanan (+30°)") { sensorDataManager.simulateGyro(0f, 30f, 45f) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PresetButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
