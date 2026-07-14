package com.example.ui

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlarmBgBottom
import com.example.ui.theme.AlarmBgMid
import com.example.ui.theme.AlarmBgTop
import com.example.ui.theme.AlarmGold
import com.example.ui.theme.AlarmGoldDeep
import com.example.ui.theme.SuccessColor
import com.example.ui.theme.MyApplicationTheme
import kotlin.math.roundToInt

class AlarmActivity : ComponentActivity() {

    companion object {
        var activeActivity: AlarmActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeActivity = this

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
        if (activeActivity == this) {
            activeActivity = null
        }
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

    // Breathing scale for the central alarm badge
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Slow ambient rotation for the decorative background glow
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgRotation"
    )

    // Aura opacity pulse
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    // Ringing wiggle for the bell icon
    val wiggleRotation by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
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
                    colors = listOf(AlarmBgTop, AlarmBgMid, AlarmBgBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient rotating glow (very subtle, purely decorative)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(420.dp)
                    .rotate(rotation)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                AlarmGold.copy(alpha = 0.05f),
                                Color.Transparent,
                                AlarmGold.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Alarm badge and Text/Card
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .scale(scale)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(AlarmGold.copy(alpha = auraAlpha), Color.Transparent)
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(AlarmBgMid, shape = CircleShape)
                                    .border(BorderStroke(1.dp, AlarmGold.copy(alpha = 0.6f)), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        brush = Brush.verticalGradient(colors = listOf(AlarmGold, AlarmGoldDeep)),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Alarm,
                                    contentDescription = "المنبه",
                                    tint = AlarmBgBottom,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(wiggleRotation)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "حَانَ الآنَ مَوْعِدُ الحِصَّةِ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AlarmGold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                border = BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            AlarmGold.copy(alpha = 0.75f),
                                            Color.White.copy(alpha = 0.35f),
                                            AlarmBgMid.copy(alpha = 0.4f)
                                        )
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = AlarmBgTop.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(AlarmBgMid, shape = RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, AlarmGold.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "يَوْمُ $day • السَّاعَةُ $formattedTime",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        color = AlarmGold,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text(
                                text = text,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 24.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Right Column: Slide to stop, snooze, footer
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SlideToStopControl(onStop = onStop)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onSnooze,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AlarmGold.copy(alpha = 0.9f)
                        ),
                        border = BorderStroke(1.5.dp, AlarmGold.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Snooze,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تَأْجِيلٌ (١٠ دَقَائِق)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "بواسطة الشيخ أحمد النمس غفر الله له ولوالديه",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AlarmGold.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing pulsing badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(scale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(AlarmGold.copy(alpha = auraAlpha), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .background(AlarmBgMid, shape = CircleShape)
                            .border(BorderStroke(2.dp, AlarmGold.copy(alpha = 0.6f)), CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .size(94.dp)
                            .background(
                                brush = Brush.verticalGradient(colors = listOf(AlarmGold, AlarmGoldDeep)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = "المنبه",
                            tint = AlarmBgBottom,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(wiggleRotation)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "حَانَ الآنَ مَوْعِدُ الحِصَّةِ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = AlarmGold,
                        fontSize = 22.sp,
                        letterSpacing = 0.4.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Premium glass card with appointment details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            border = BorderStroke(
                                1.5.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        AlarmGold.copy(alpha = 0.75f),
                                        Color.White.copy(alpha = 0.35f),
                                        AlarmBgMid.copy(alpha = 0.4f)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = AlarmBgTop.copy(alpha = 0.4f)
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
                        Box(
                            modifier = Modifier
                                .background(AlarmBgMid, shape = RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, AlarmGold.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "يَوْمُ $day • السَّاعَةُ $formattedTime",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    color = AlarmGold,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = text,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 27.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                lineHeight = 36.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                // Interactive slide-to-stop control (primary action), plus a compact snooze button.
                // Functionally identical to before: stop / snooze(10 min) — just presented in a
                // more modern, tactile way that's harder to trigger by accident.
                SlideToStopControl(onStop = onStop)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AlarmGold.copy(alpha = 0.9f)
                    ),
                    border = BorderStroke(1.5.dp, AlarmGold.copy(alpha = 0.45f)),
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
                        Icon(
                            imageVector = Icons.Filled.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تَأْجِيلٌ (١٠ دَقَائِق)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "بواسطة الشيخ أحمد النمس غفر الله له ولوالديه",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AlarmGold.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * A modern "slide to stop" control: the primary way alarm clock apps prevent an alarm
 * from being dismissed by an accidental tap while still using a single, clear gesture.
 * Dragging the gold handle across the track calls [onStop] — same underlying action as
 * the old "إيقاف التنبيه" button, just presented as an interactive, premium control.
 */
@Composable
private fun SlideToStopControl(onStop: () -> Unit) {
    val trackHeight = 64.dp
    val handleSize = 56.dp
    var trackWidthPx by remember { mutableStateOf(0f) }
    var dragOffsetPx by remember { mutableStateOf(0f) }
    var triggered by remember { mutableStateOf(false) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    val maxOffsetPx = remember(trackWidthPx) {
        val handlePx = with(density) { handleSize.toPx() }
        (trackWidthPx - handlePx).coerceAtLeast(0f)
    }

    val animatedOffset by animateFloatAsState(
        targetValue = dragOffsetPx,
        animationSpec = if (triggered) tween(150) else spring(),
        label = "handleOffset"
    )

    val dragProgress = if (maxOffsetPx > 0f) (animatedOffset / maxOffsetPx) else 0f
    val fillWidthDp = with(density) { (animatedOffset + (handleSize.toPx() / 2)).toDp() }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .background(AlarmBgTop.copy(alpha = 0.55f), shape = RoundedCornerShape(32.dp))
                .border(BorderStroke(1.5.dp, AlarmGold.copy(alpha = 0.35f)), RoundedCornerShape(32.dp))
                .onSizeChanged { trackWidthPx = it.width.toFloat() },
            contentAlignment = Alignment.CenterStart
        ) {
            // Dynamic color/gradient behind the dragged button (green success interactive indicator)
            if (animatedOffset > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(fillWidthDp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    SuccessColor.copy(alpha = 0.15f),
                                    SuccessColor.copy(alpha = 0.85f * dragProgress)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                )
            }

            Text(
                text = "اِسْحَبْ لِإِيقَافِ التَّنْبِيهِ »",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .offset { androidx.compose.ui.unit.IntOffset(animatedOffset.roundToInt(), 0) }
                    .size(handleSize)
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(AlarmGold, AlarmGoldDeep)),
                        shape = CircleShape
                    )
                    .pointerInput(maxOffsetPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffsetPx >= maxOffsetPx * 0.82f) {
                                    triggered = true
                                    dragOffsetPx = maxOffsetPx
                                    onStop()
                                } else {
                                    dragOffsetPx = 0f
                                }
                            },
                            onHorizontalDrag = { change, delta ->
                                change.consume()
                                dragOffsetPx = (dragOffsetPx + delta).coerceIn(0f, maxOffsetPx)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "إيقاف التنبيه",
                    tint = AlarmBgBottom,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(180f)
                )
            }
        }
    }
}
