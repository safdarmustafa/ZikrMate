package com.falahpro.app.dua

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.falahpro.app.dua.repository.DuaRepository

private val DuaBgTop = Color(0xFF241612)
private val DuaBgBottom = Color(0xFF140C09)
private val DuaGold = Color(0xFFE2C07A)
private val DuaCard = Color(0xFF2C1B16)

@Composable
fun DuaDetailScreen(
    fileName: String,
    duaId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { DuaRepository() }

    val dua = remember(fileName, duaId) {
        repository.loadDuas(context, fileName).firstOrNull { it.id == duaId }
    }

    val categoryName = remember(fileName) {
        fileName
            .removeSuffix(".json")
            .replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DuaBgTop, DuaBgBottom)))
            .padding(16.dp)
    ) {
        Text(
            text = "← Back",
            color = DuaGold,
            fontSize = 15.sp,
            modifier = Modifier.clickable(onClick = onBack)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = categoryName,
            color = DuaGold.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (dua == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "No duas available.",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = dua.title,
                    color = DuaGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = 32.sp
                )

                DetailSectionCard {
                    DetailLabel("Arabic")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = dua.arabic,
                        color = Color(0xFFF5E6C8),
                        fontSize = 26.sp,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center,
                        style = TextStyle(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DetailSectionCard {
                    DetailLabel("Transliteration")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = dua.transliteration,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                DetailSectionCard {
                    DetailLabel("Translation")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = dua.translation,
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    )
                }

                DetailSectionCard {
                    DetailLabel("When To Recite")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = dua.whenToRecite,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                DetailSectionCard {
                    DetailLabel("Reference")
                    Spacer(modifier = Modifier.height(14.dp))

                    ReferenceRow(label = "Book", value = dua.reference.book)
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DuaGold.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))
                    ReferenceRow(label = "Hadith", value = dua.reference.hadith)
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DuaGold.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))
                    ReferenceRow(label = "Number", value = dua.reference.number)
                }

                DetailSectionCard {
                    DetailLabel("Repeat Count")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (dua.repeat == 1) "1 time" else "${dua.repeat} times",
                        color = DuaGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DuaCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = DuaGold.copy(alpha = 0.7f),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun ReferenceRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.LightGray.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        Text(
            text = value.ifBlank { "—" },
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
