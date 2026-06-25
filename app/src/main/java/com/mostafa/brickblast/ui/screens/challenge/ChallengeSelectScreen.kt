package com.mostafa.brickblast.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.domain.model.ChallengeConfig
import com.mostafa.brickblast.domain.model.ChallengeProgress
import com.mostafa.brickblast.ui.accessibility.screenHeading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeSelectScreen(
    progress: ChallengeProgress,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val levels = ChallengeConfig.levelsOnPage(currentPage).toList()
    val pageLabel = "Page ${currentPage + 1} / ${ChallengeConfig.totalPages}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenge Mode", modifier = Modifier.screenHeading()) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Navigate back" }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                "Clear all bricks to win • ${ChallengeConfig.TOTAL_LEVELS} levels",
                color = MaterialTheme.colorScheme.onBackground.copy(0.65f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Unlocked: ${progress.highestUnlocked} / ${ChallengeConfig.TOTAL_LEVELS}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Page navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous page")
                }
                Text(
                    pageLabel,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .screenHeading()
                )
                IconButton(
                    onClick = {
                        if (currentPage < ChallengeConfig.totalPages - 1) currentPage++
                    },
                    enabled = currentPage < ChallengeConfig.totalPages - 1
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next page")
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(levels) { level ->
                    ChallengeLevelCard(
                        level = level,
                        unlocked = progress.isUnlocked(level),
                        completed = progress.isCompleted(level),
                        onClick = { onSelectLevel(level) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeLevelCard(
    level: Int,
    unlocked: Boolean,
    completed: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        completed -> Color(0xFF1B5E20)
        unlocked -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val textColor = when {
        !unlocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
        completed -> Color(0xFF69F0AE)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val a11yLabel = when {
        !unlocked -> "Level $level, locked"
        completed -> "Level $level, completed"
        else -> "Level $level"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = a11yLabel
                if (unlocked) {
                    role = Role.Button
                } else {
                    disabled()
                }
            }
            .clickable(enabled = unlocked) { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$level",
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.semantics { invisibleToUser() }
            )
            if (!unlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 6.dp)
                        .size(14.dp)
                )
            } else if (completed) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF69F0AE),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp)
                        .size(14.dp)
                )
            }
        }
    }
}
