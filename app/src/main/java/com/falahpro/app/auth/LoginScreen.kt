package com.falahpro.app.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falahpro.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.math.cos
import kotlin.math.sin

private object LoginColors {
    val EmeraldDeep = Color(0xFF0A3328)
    val Emerald = Color(0xFF145A45)
    val EmeraldMid = Color(0xFF1F6B52)
    val EmeraldSoft = Color(0xFF2F8A68)
    val Forest = Color(0xFF0E4032)
    val Gold = Color(0xFFC4A35A)
    val GoldBright = Color(0xFFD4B978)
    val GoldSoft = Color(0xFFE8D9A8)
    val Ivory = Color(0xFFF8F4EC)
    val Cream = Color(0xFFF3EDE1)
    val CreamCard = Color(0xFCFFFCF0)
    val Mist = Color(0xFFE4EDE7)
    val TextPrimary = Color(0xFF102820)
    val TextSecondary = Color(0xFF4A5C54)
    val TextMuted = Color(0xFF6B7A72)

    val DarkBgTop = Color(0xFF061510)
    val DarkBgMid = Color(0xFF0B241C)
    val DarkBgBottom = Color(0xFF12352A)
    val DarkCard = Color(0xE6143228)
    val DarkText = Color(0xFFF5F0E6)
    val DarkMuted = Color(0xFFA8B8B0)
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val darkTheme = isSystemInDarkTheme()

    var isSigningIn by remember { mutableStateOf(false) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    isSigningIn = false
                    if (authResult.isSuccessful) {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (_: Exception) {
            isSigningIn = false
            Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            onLoginSuccess()
        }
    }

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entered = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = EaseOutCubic),
        label = "loginFadeIn"
    )
    val contentSlide by animateFloatAsState(
        targetValue = if (entered) 0f else 28f,
        animationSpec = tween(durationMillis = 1100, easing = EaseOutCubic),
        label = "loginSlideUp"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "heroFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroFloatOffset"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "buttonPress"
    )

    val titleColor = if (darkTheme) LoginColors.DarkText else LoginColors.EmeraldDeep
    val taglineColor = if (darkTheme) {
        LoginColors.GoldSoft.copy(alpha = 0.72f)
    } else {
        LoginColors.Gold.copy(alpha = 0.82f)
    }
    val subtitleColor = if (darkTheme) {
        LoginColors.DarkMuted.copy(alpha = 0.7f)
    } else {
        LoginColors.TextSecondary.copy(alpha = 0.72f)
    }
    val cardColor = if (darkTheme) LoginColors.DarkCard else Color(0xF7FFFCF0)
    val mutedColor = if (darkTheme) LoginColors.DarkMuted else LoginColors.TextMuted

    Box(modifier = Modifier.fillMaxSize()) {
        IslamicPatternBackground(
            modifier = Modifier.fillMaxSize(),
            darkTheme = darkTheme
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .alpha(contentAlpha)
                .offset(y = contentSlide.dp)
                .padding(horizontal = 28.dp)
                .padding(top = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .offset(y = floatOffset.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        LoginColors.GoldSoft.copy(alpha = glowPulse * 0.9f),
                                        LoginColors.EmeraldSoft.copy(alpha = glowPulse * 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        if (darkTheme) {
                                            LoginColors.EmeraldMid.copy(alpha = 0.35f)
                                        } else {
                                            Color.White.copy(alpha = 0.55f)
                                        },
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    PremiumIslamicHero(
                        modifier = Modifier.size(168.dp),
                        darkTheme = darkTheme,
                        glowPulse = glowPulse
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Falah Pro",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 48.sp
                    ),
                    color = titleColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Built for Every Muslim",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.6.sp
                    ),
                    color = taglineColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(1.5.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    LoginColors.Gold.copy(alpha = 0.75f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Faith. Prayer. Reflection.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.8.sp
                    ),
                    color = subtitleColor,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = LoginColors.Forest.copy(alpha = 0.14f),
                            spotColor = LoginColors.Gold.copy(alpha = 0.10f)
                        ),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (darkTheme) 0.12f else 0.65f),
                                LoginColors.GoldSoft.copy(alpha = if (darkTheme) 0.18f else 0.35f),
                                Color.White.copy(alpha = if (darkTheme) 0.06f else 0.25f)
                            )
                        )
                    ),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 26.dp, vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.1.sp
                            ),
                            color = if (darkTheme) LoginColors.DarkText else LoginColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your journey of faith begins here",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = mutedColor.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = {
                                if (!isSigningIn) {
                                    isSigningIn = true
                                    launcher.launch(googleSignInClient.signInIntent)
                                }
                            },
                            enabled = !isSigningIn,
                            interactionSource = interactionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .scale(buttonScale)
                                .semantics { contentDescription = "Sign in with Google" }
                                .shadow(
                                    elevation = if (isPressed) 2.dp else 8.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.06f),
                                    spotColor = LoginColors.Emerald.copy(alpha = 0.18f)
                                ),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (darkTheme) {
                                    Color.White.copy(alpha = 0.08f)
                                } else {
                                    Color(0xFFE8E4DC)
                                }
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (darkTheme) Color(0xFFF7F3EA) else Color.White,
                                contentColor = LoginColors.TextPrimary,
                                disabledContainerColor = if (darkTheme) {
                                    Color(0xFFF7F3EA).copy(alpha = 0.72f)
                                } else {
                                    Color.White.copy(alpha = 0.72f)
                                },
                                disabledContentColor = LoginColors.TextPrimary.copy(alpha = 0.7f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = LoginColors.Emerald
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Signing in…",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = LoginColors.TextPrimary
                                )
                            } else {
                                GoogleMark()
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.15.sp
                                    ),
                                    color = LoginColors.TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Protected by Google secure sign-in",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = mutedColor.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Assalamu Alaikum",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    ),
                    color = (if (darkTheme) LoginColors.GoldSoft else LoginColors.EmeraldMid)
                        .copy(alpha = 0.48f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PremiumIslamicHero(
    modifier: Modifier = Modifier,
    darkTheme: Boolean,
    glowPulse: Float
) {
    val lineColor = if (darkTheme) {
        LoginColors.GoldSoft.copy(alpha = 0.9f)
    } else {
        LoginColors.EmeraldDeep.copy(alpha = 0.88f)
    }
    val fillColor = if (darkTheme) {
        LoginColors.EmeraldSoft.copy(alpha = 0.22f)
    } else {
        LoginColors.Emerald.copy(alpha = 0.10f)
    }
    val accent = LoginColors.Gold.copy(alpha = 0.55f + glowPulse * 0.35f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val baseY = h * 0.78f

        // Soft ground glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(cx, baseY),
                radius = w * 0.42f
            ),
            radius = w * 0.42f,
            center = Offset(cx, baseY)
        )

        // Crescent
        val crescentCenter = Offset(cx + w * 0.22f, h * 0.22f)
        drawCircle(
            color = accent,
            radius = w * 0.07f,
            center = crescentCenter,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(
            color = if (darkTheme) LoginColors.DarkBgMid else LoginColors.Ivory,
            radius = w * 0.055f,
            center = Offset(crescentCenter.x + w * 0.02f, crescentCenter.y - w * 0.01f)
        )

        // Stars
        drawStarOrnament(
            center = Offset(cx - w * 0.28f, h * 0.18f),
            radius = 5.dp.toPx(),
            color = accent.copy(alpha = 0.7f)
        )
        drawStarOrnament(
            center = Offset(cx + w * 0.08f, h * 0.12f),
            radius = 3.5.dp.toPx(),
            color = accent.copy(alpha = 0.55f)
        )
        drawStarOrnament(
            center = Offset(cx - w * 0.12f, h * 0.28f),
            radius = 2.8.dp.toPx(),
            color = accent.copy(alpha = 0.45f)
        )

        // Central dome
        val domeWidth = w * 0.34f
        val domeLeft = cx - domeWidth / 2f
        val domePath = Path().apply {
            moveTo(domeLeft, baseY - h * 0.18f)
            quadraticTo(cx, baseY - h * 0.42f, domeLeft + domeWidth, baseY - h * 0.18f)
            lineTo(domeLeft + domeWidth, baseY)
            lineTo(domeLeft, baseY)
            close()
        }
        drawPath(domePath, color = fillColor)
        drawPath(domePath, color = lineColor, style = Stroke(width = 2.dp.toPx()))

        // Dome finial
        drawLine(
            color = lineColor,
            start = Offset(cx, baseY - h * 0.42f),
            end = Offset(cx, baseY - h * 0.50f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = accent,
            radius = 3.dp.toPx(),
            center = Offset(cx, baseY - h * 0.52f)
        )

        // Side minarets
        drawMinaret(
            centerX = cx - w * 0.28f,
            baseY = baseY,
            height = h * 0.38f,
            width = w * 0.045f,
            lineColor = lineColor,
            fillColor = fillColor,
            accent = accent
        )
        drawMinaret(
            centerX = cx + w * 0.28f,
            baseY = baseY,
            height = h * 0.38f,
            width = w * 0.045f,
            lineColor = lineColor,
            fillColor = fillColor,
            accent = accent
        )

        // Base platform
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(cx - w * 0.38f, baseY),
            size = Size(w * 0.76f, h * 0.05f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(cx - w * 0.38f, baseY),
            size = Size(w * 0.76f, h * 0.05f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )
    }
}

private fun DrawScope.drawMinaret(
    centerX: Float,
    baseY: Float,
    height: Float,
    width: Float,
    lineColor: Color,
    fillColor: Color,
    accent: Color
) {
    val left = centerX - width / 2f
    drawRoundRect(
        color = fillColor,
        topLeft = Offset(left, baseY - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2f)
    )
    drawRoundRect(
        color = lineColor,
        topLeft = Offset(left, baseY - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2f),
        style = Stroke(width = 1.6.dp.toPx())
    )
    drawCircle(
        color = accent,
        radius = width * 0.55f,
        center = Offset(centerX, baseY - height),
        style = Stroke(width = 1.8.dp.toPx())
    )
    drawLine(
        color = lineColor,
        start = Offset(centerX, baseY - height - width * 0.55f),
        end = Offset(centerX, baseY - height - width * 1.3f),
        strokeWidth = 1.6.dp.toPx(),
        cap = StrokeCap.Round
    )
}

@Composable
private fun GoogleMark() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.size(width = 22.dp, height = 22.dp)
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val r = size.minDimension / 2f
            val stroke = 3.2.dp.toPx()
            val colors = listOf(
                Color(0xFF4285F4),
                Color(0xFF34A853),
                Color(0xFFFBBC05),
                Color(0xFFEA4335)
            )
            // Simplified multicolor Google "G" ring
            drawArc(
                color = colors[0],
                startAngle = -20f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
                size = Size(r * 2, r * 2),
                topLeft = Offset.Zero
            )
            drawArc(
                color = colors[1],
                startAngle = 80f,
                sweepAngle = 80f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
                size = Size(r * 2, r * 2),
                topLeft = Offset.Zero
            )
            drawArc(
                color = colors[2],
                startAngle = 160f,
                sweepAngle = 70f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
                size = Size(r * 2, r * 2),
                topLeft = Offset.Zero
            )
            drawArc(
                color = colors[3],
                startAngle = 230f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
                size = Size(r * 2, r * 2),
                topLeft = Offset.Zero
            )
            drawRect(
                color = colors[0],
                topLeft = Offset(r - 0.5.dp.toPx(), r - stroke / 2f),
                size = Size(r + 1.dp.toPx(), stroke)
            )
        }
    }
}

@Composable
private fun IslamicPatternBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    val top = if (darkTheme) LoginColors.DarkBgTop else LoginColors.Ivory
    val mid = if (darkTheme) LoginColors.DarkBgMid else LoginColors.Cream
    val bottom = if (darkTheme) LoginColors.DarkBgBottom else LoginColors.Mist
    val pattern = if (darkTheme) {
        LoginColors.Gold.copy(alpha = 0.045f)
    } else {
        LoginColors.Emerald.copy(alpha = 0.04f)
    }
    val ornament = if (darkTheme) {
        LoginColors.GoldSoft.copy(alpha = 0.06f)
    } else {
        LoginColors.Gold.copy(alpha = 0.05f)
    }
    val glow = if (darkTheme) {
        LoginColors.EmeraldSoft.copy(alpha = 0.22f)
    } else {
        LoginColors.EmeraldSoft.copy(alpha = 0.16f)
    }

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(top, mid, bottom)
            )
        )

        // Large ambient radial washes for depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow, Color.Transparent),
                center = Offset(size.width * 0.18f, size.height * 0.12f),
                radius = size.minDimension * 0.75f
            ),
            radius = size.minDimension * 0.75f,
            center = Offset(size.width * 0.18f, size.height * 0.12f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LoginColors.GoldSoft.copy(alpha = if (darkTheme) 0.10f else 0.14f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.85f, size.height * 0.28f),
                radius = size.minDimension * 0.55f
            ),
            radius = size.minDimension * 0.55f,
            center = Offset(size.width * 0.85f, size.height * 0.28f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LoginColors.Forest.copy(alpha = if (darkTheme) 0.28f else 0.08f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.95f),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width * 0.5f, size.height * 0.95f)
        )

        // Sparse geometric lattice — calm, not noisy
        val step = 88.dp.toPx()
        var y = step
        var row = 0
        while (y < size.height * 0.72f) {
            var x = if (row % 2 == 0) step * 0.5f else step
            while (x < size.width) {
                drawCircle(
                    color = pattern,
                    radius = 7.dp.toPx(),
                    center = Offset(x, y),
                    style = Stroke(width = 1.dp.toPx())
                )
                if ((row + (x / step).toInt()) % 3 == 0) {
                    drawStarOrnament(
                        center = Offset(x, y),
                        radius = 14.dp.toPx(),
                        color = ornament
                    )
                }
                x += step
            }
            y += step
            row++
        }

        // Soft distant mosque skyline
        val silhouette = if (darkTheme) {
            LoginColors.EmeraldSoft.copy(alpha = 0.10f)
        } else {
            LoginColors.EmeraldDeep.copy(alpha = 0.06f)
        }
        val baseY = size.height * 0.90f
        val skyline = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, baseY)
            lineTo(size.width * 0.10f, baseY)
            lineTo(size.width * 0.15f, baseY - 40.dp.toPx())
            lineTo(size.width * 0.20f, baseY)
            lineTo(size.width * 0.35f, baseY)
            lineTo(size.width * 0.40f, baseY - 22.dp.toPx())
            lineTo(size.width * 0.45f, baseY - 64.dp.toPx())
            lineTo(size.width * 0.50f, baseY - 22.dp.toPx())
            lineTo(size.width * 0.55f, baseY)
            lineTo(size.width * 0.72f, baseY)
            lineTo(size.width * 0.78f, baseY - 36.dp.toPx())
            lineTo(size.width * 0.84f, baseY)
            lineTo(size.width, baseY)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(skyline, color = silhouette)
    }
}

private fun DrawScope.drawStarOrnament(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path()
    val points = 8
    for (i in 0 until points * 2) {
        val angle = (Math.PI / points) * i - Math.PI / 2
        val r = if (i % 2 == 0) radius else radius * 0.42f
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = 1.dp.toPx()))
}
