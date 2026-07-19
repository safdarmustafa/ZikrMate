package com.falahpro.app.tasbih

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falahpro.app.R
import com.falahpro.app.data.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun TasbihScreen(
    onProfileClick: () -> Unit = {},
    onReady: () -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isMuted by remember { mutableStateOf(false) }
    var hasSignaledReady by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }
    var hasCountLoaded by remember { mutableStateOf(false) }

    fun signalReady() {
        if (!hasSignaledReady && hasLaidOut && hasCountLoaded) {
            hasSignaledReady = true
            onReady()
        }
    }

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val clickSoundId = remember {
        soundPool.load(context, R.raw.tasbihclick, 1)
    }

    fun playClick() {
        if (!isMuted) {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    val transition = rememberInfiniteTransition(label = "")
    val offset by transition.animateFloat(
        0f, 900f,
        infiniteRepeatable(tween(18000), RepeatMode.Reverse),
        label = ""
    )

    val gradient = Brush.linearGradient(
        listOf(Color(0xFF2A1C18), Color(0xFF3E2A24), Color(0xFF1A120F)),
        start = Offset.Zero,
        end = Offset(offset, offset)
    )

    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Assalamu Alaikum, Good Morning"
        in 12..16 -> "Assalamu Alaikum, Good Afternoon"
        in 17..20 -> "Assalamu Alaikum, Good Evening"
        else -> "Peaceful Night"
    }

    val quotes = listOf(
        "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ" to
                "Surely in the remembrance of Allah do hearts find peace",
        "إِنَّ مَعَ الْعُسْرِ يُسْرًا" to
                "Indeed, with hardship comes ease"
    )

    var quoteIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            quoteIndex = (quoteIndex + 1) % quotes.size
        }
    }

    val dhikrList = listOf(
        "سُبْحَانَ اللَّهِ",
        "الْحَمْدُ لِلَّهِ",
        "اللَّهُ أَكْبَرُ"
    )

    var selectedDhikr by remember { mutableStateOf(dhikrList[0]) }
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedDhikr) {
        DataStoreManager.getCount(context, selectedDhikr)
            .collect {
                count = it
                hasCountLoaded = true
                signalReady()
            }
    }

    // Fallback if DataStore is slow — still release splash.
    LaunchedEffect(Unit) {
        delay(450)
        hasCountLoaded = true
        signalReady()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
            .onGloballyPositioned {
                hasLaidOut = true
                signalReady()
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            PremiumProfileButton(
                onClick = onProfileClick,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Text(
                text = "Tasbih Counter",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE2C07A),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 20.dp)
        ) {

            Text(
                "بِسْمِ ٱللَّٰهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2C07A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))
            Text(greeting, fontSize = 14.sp, color = Color(0xFFD0C4BC))

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White.copy(0.05f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = quoteIndex, label = "") { index ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            quotes[index].first,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2C07A),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            quotes[index].second,
                            fontSize = 15.sp,
                            color = Color.White.copy(0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                dhikrList.forEach { dhikr ->
                    val selected = dhikr == selectedDhikr
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected)
                                    Brush.linearGradient(
                                        listOf(Color(0xFFE2C07A), Color(0xFFB89B5E))
                                    )
                                else
                                    Brush.linearGradient(
                                        listOf(Color(0xFF3E2A24), Color(0xFF2A1C18))
                                    ),
                                RoundedCornerShape(30.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedDhikr = dhikr
                                playClick()
                            }
                            .padding(horizontal = 22.dp, vertical = 12.dp)
                    ) {
                        Text(
                            dhikr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color(0xFF2A1C18) else Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(Color.White.copy(0.12f), CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        playClick()
                        count++
                        scope.launch {
                            DataStoreManager.saveCount(context, selectedDhikr, count)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("$count", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF3E2A24), RoundedCornerShape(22.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        playClick()
                        count = 0
                        scope.launch {
                            DataStoreManager.saveCount(context, selectedDhikr, 0)
                        }
                    }
                    .padding(horizontal = 26.dp, vertical = 10.dp)
            ) {
                Text("Reset", color = Color.White)
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF3E2A24), RoundedCornerShape(22.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isMuted = !isMuted
                    }
                    .padding(horizontal = 26.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isMuted) "🔇 UnMute" else "🔊 Mute",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PremiumProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gold = Color(0xFFE2C07A)
    val goldDeep = Color(0xFFB89B5E)
    val maroon = Color(0xFF2A1C18)

    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = gold.copy(alpha = 0.35f),
                spotColor = gold.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(gold, goldDeep, Color(0xFFD4AF37))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        gold.copy(alpha = 0.2f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .semantics { contentDescription = "Profile" }
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3E2A24), maroon)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                val w = size.width
                val h = size.height
                val iconColor = gold

                // Head
                drawCircle(
                    color = iconColor,
                    radius = w * 0.22f,
                    center = Offset(w * 0.5f, h * 0.32f)
                )

                // Shoulders / torso
                val torso = Path().apply {
                    moveTo(w * 0.18f, h * 0.92f)
                    cubicTo(
                        w * 0.18f, h * 0.62f,
                        w * 0.32f, h * 0.55f,
                        w * 0.5f, h * 0.55f
                    )
                    cubicTo(
                        w * 0.68f, h * 0.55f,
                        w * 0.82f, h * 0.62f,
                        w * 0.82f, h * 0.92f
                    )
                    close()
                }
                drawPath(torso, color = iconColor, style = Fill)

                // Soft ring accent
                drawCircle(
                    color = iconColor.copy(alpha = 0.35f),
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
