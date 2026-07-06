package com.example.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightTeal
import com.example.ui.theme.MediumTeal
import com.example.ui.theme.MyApplicationTheme

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lockscreen and wake screen up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        
        enableEdgeToEdge()

        val appointmentText = intent.getStringExtra("appointment_text") ?: "حصة قرآنية"
        val appointmentDay = intent.getStringExtra("appointment_day") ?: ""
        val appointmentTime = intent.getStringExtra("appointment_time") ?: ""
        val ringtoneUri = intent.getStringExtra("ringtone_uri") ?: ""

        setContent {
            MyApplicationTheme {
                AlarmScreen(
                    text = appointmentText,
                    day = appointmentDay,
                    time = appointmentTime,
                    onStop = {
                        AlarmService.activeService?.stopAlarmService()
                        finish()
                    },
                    onSnooze = {
                        AlarmService.activeService?.snoozeAlarmService(
                            this@AlarmActivity,
                            appointmentText,
                            appointmentDay,
                            appointmentTime,
                            ringtoneUri
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        // Clicking back stops the alarm service and closes it
        AlarmService.activeService?.stopAlarmService()
        super.onBackPressed()
    }
}

@Composable
fun AlarmScreen(
    text: String,
    day: String,
    time: String,
    onStop: () -> Unit,
    onSnooze: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Scale animation for the main alarm circle
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Soft rotation animation for the background Islamic patterns
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgRotation"
    )

    // Pulsing aura opacity
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    // Rapid wiggling / ringing animation for the alarm bell icon
    val wiggleRotation by infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggleRotation"
    )

    val formattedTime = remember(time) {
        try {
            val parts = time.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: 12
                val m = parts[1].toIntOrNull() ?: 0
                val amPm = if (h >= 12) "م" else "ص"
                val h12 = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                String.format("%d:%02d %s", h12, m, amPm)
            } else {
                time
            }
        } catch (e: Exception) {
            time
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF001F1A), // Extremely rich deep forest green
                        Color(0xFF003D33), // Deep teal
                        Color(0xFF00120F)  // Dark shadowed green
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Animated Background Elements (Islamic Geometric Stars)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm, // Using it dynamically as a shape or we can use custom icons
                contentDescription = null,
                tint = Color(0xFFFFD54F).copy(alpha = 0.03f),
                modifier = Modifier
                    .size(450.dp)
                    .scale(scale * 1.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Glowing Pulsing Circular Header Group
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Outer gold aura ring 1
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD54F).copy(alpha = auraAlpha), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Outer gold aura ring 2
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(Color(0xFF004D40), shape = CircleShape)
                        .border(BorderStroke(2.dp, Color(0xFFFFD54F).copy(alpha = 0.6f)), CircleShape)
                )

                // Central high-contrast gold coin
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = "المنبه",
                        tint = Color(0xFF001F1A),
                        modifier = Modifier
                            .size(50.dp)
                            .rotate(wiggleRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Header with proper typography hierarchy
            Text(
                text = "⏱️ حَانَ الآنَ مَوْعِدُ الحِصَّةِ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD54F),
                    fontSize = 21.sp,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Glassmorphic Card (Deep translucent teal background with thick gold and white glass accents)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(
                            1.5.dp, 
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFD54F).copy(alpha = 0.8f), 
                                    Color.White.copy(alpha = 0.4f), 
                                    Color(0xFF00796B).copy(alpha = 0.4f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF001F1A).copy(alpha = 0.38f)
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(26.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Date & Time badge with custom visual weight
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF004D40), shape = RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "يَوْمُ $day • السَّاعَةُ $formattedTime",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Main Appointment Text in extremely bold, crisp and huge size
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            lineHeight = 38.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action Buttons Group (Interactive & Polished layout)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dismiss / Stop Button (Prominent Gold Accent)
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F),
                        contentColor = Color(0xFF001F1A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "🛑 إِيقَافُ التَّنْبِيهِ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        )
                    }
                }

                // Snooze Button (Sleek Glassmorphic Outlined Button)
                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF80CBC4)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFF80CBC4)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "💤 تَأْجِيلٌ (١٠ دَقَائِق)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sheikh Ahmed El Nems Signature / Rights Credit Label
            Text(
                text = "بواسطة الشيخ أحمد النمس غفر الله له ولوالديه",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFFFE082),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
