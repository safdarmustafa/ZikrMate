package com.falahpro.app.dua

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.falahpro.app.dua.json.JsonCategory
import com.falahpro.app.dua.repository.DuaRepository

private val DuaBgTop = Color(0xFF241612)
private val DuaBgBottom = Color(0xFF140C09)
private val DuaGold = Color(0xFFE2C07A)
private val DuaCard = Color(0xFF2C1B16)

@Composable
fun DuaLibraryScreen(
    onCategoryClick: (JsonCategory) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { DuaRepository() }
    val categories = remember { repository.loadCategories(context) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DuaBgTop, DuaBgBottom)))
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Dua Library",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DuaGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Essential authentic duas for every Muslim.",
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (categories.isEmpty()) {
            item {
                Text(
                    text = "No duas available.",
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(categories, key = { it.id }) { category ->
                val duaCount = remember(category.file) {
                    repository.loadDuas(context, category.file).size
                }

                CategoryCard(
                    category = category,
                    duaCount = duaCount,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: JsonCategory,
    duaCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DuaCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.icon,
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    color = DuaGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = category.description,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (duaCount == 1) "1 dua" else "$duaCount duas",
                    color = DuaGold.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "$duaCount",
                color = DuaGold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
