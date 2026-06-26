package com.zikrmate.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zikrmate.app.data.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {

    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    val name = user?.displayName ?: "Zikr Companion"
    val email = user?.email ?: "No Email"
    val photoUrl = user?.photoUrl

    var totalDhikr by remember { mutableIntStateOf(0) }
    var mostRecited by remember { mutableStateOf("Calculating...") }
    var accountSince by remember { mutableStateOf("Loading...") }

    val dhikrList = listOf(
        "سُبْحَانَ اللَّهِ",
        "الْحَمْدُ لِلَّهِ",
        "اللَّهُ أَكْبَرُ"
    )

    LaunchedEffect(Unit) {

        val subhan = DataStoreManager.getCount(context, dhikrList[0]).first()
        val alham = DataStoreManager.getCount(context, dhikrList[1]).first()
        val akbar = DataStoreManager.getCount(context, dhikrList[2]).first()

        totalDhikr = subhan + alham + akbar

        val max = maxOf(subhan, alham, akbar)

        mostRecited = when (max) {
            subhan -> dhikrList[0]
            alham -> dhikrList[1]
            akbar -> dhikrList[2]
            else -> "Start your journey"
        }
    }

    LaunchedEffect(Unit) {
        DataStoreManager.saveAccountCreationDate(context)

        val rawDate =
            DataStoreManager.getAccountCreationDate(context).first()

        rawDate?.let {
            val formatted = LocalDate.parse(it)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            accountSince = formatted
        } ?: run {
            accountSince = "Unknown"
        }
    }

    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A120F),
            Color(0xFF2A1C18),
            Color(0xFF3E2A24)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2C07A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.first().toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A120F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2C07A)
            )

            Text(
                text = email,
                fontSize = 14.sp,
                color = Color.White.copy(0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            ProfileStat("Total Dhikr", totalDhikr.toString())
            Spacer(modifier = Modifier.height(20.dp))
            ProfileStat("Most Recited", mostRecited)
            Spacer(modifier = Modifier.height(20.dp))
            ProfileStat("Account Since", accountSince)

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onLogout) {
                Text(
                    text = "Logout",
                    color = Color(0xFFE2C07A),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileStat(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(0.05f),
                RoundedCornerShape(20.dp)
            )
            .padding(vertical = 18.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = Color.White.copy(0.6f)
        )
    }
}
