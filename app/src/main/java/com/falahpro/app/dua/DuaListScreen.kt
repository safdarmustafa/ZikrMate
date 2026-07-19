package com.falahpro.app.dua

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falahpro.app.dua.json.JsonDua
import com.falahpro.app.dua.repository.DuaRepository

private val DuaBgTop = Color(0xFF241612)
private val DuaBgBottom = Color(0xFF140C09)
private val DuaGold = Color(0xFFE2C07A)
private val DuaCard = Color(0xFF2C1B16)

@Composable
fun DuaListScreen(
    fileName: String,
    onBack: () -> Unit,
    onDuaClick: (JsonDua) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { DuaRepository() }

    val duas = remember(fileName) {
        repository.loadDuas(context = context, fileName = fileName)
    }

    val categoryTitle = remember(fileName) {
        fileName
            .removeSuffix(".json")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DuaBgTop, DuaBgBottom)))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "← Back",
                color = DuaGold,
                fontSize = 15.sp,
                modifier = Modifier.clickable(onClick = onBack)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = categoryTitle,
                color = DuaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (duas.isEmpty()) "0 Duas" else "${duas.size} Duas",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (duas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No duas available.",
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            items(duas, key = { it.id }) { dua ->
                DuaListItemCard(
                    dua = dua,
                    onClick = { onDuaClick(dua) }
                )
            }
        }
    }
}

@Composable
private fun DuaListItemCard(
    dua: JsonDua,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DuaCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = dua.title,
                color = DuaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dua.whenToRecite,
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dua.reference.book,
                color = DuaGold.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
