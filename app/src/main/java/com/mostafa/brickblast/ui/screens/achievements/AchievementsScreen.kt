package com.mostafa.brickblast.ui.screens.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.ui.accessibility.screenHeading
import com.mostafa.brickblast.ui.components.SecondaryButton
import com.mostafa.brickblast.ui.util.AchievementShareCard
import com.mostafa.brickblast.ui.util.AchievementShareHelper
import com.mostafa.brickblast.ui.util.achievementMetricEmoji
import com.mostafa.brickblast.ui.util.achievementProgressLabel
import com.mostafa.brickblast.ui.util.localizedDescription
import com.mostafa.brickblast.ui.util.localizedTitle
import com.mostafa.brickblast.ui.viewmodel.AchievementsViewModel
import kotlinx.coroutines.delay

private const val EARNED_PREVIEW_COUNT = 3
private const val LOCKED_PREVIEW_COUNT = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val earned = achievements
        .filter { it.unlocked }
        .sortedByDescending { it.unlockedAt ?: 0L }
    val locked = achievements.filter { !it.unlocked }
    val next = locked.firstOrNull()
    val lockedRest = if (next != null) locked.drop(1) else locked
    var showAllEarned by remember { mutableStateOf(false) }
    var showAllLocked by remember { mutableStateOf(false) }
    val visibleEarned = if (showAllEarned) earned else earned.take(EARNED_PREVIEW_COUNT)
    val visibleLocked = if (showAllLocked) lockedRest else lockedRest.take(LOCKED_PREVIEW_COUNT)
    val navigateBackLabel = stringResource(R.string.navigate_back)
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val graphicsLayer = rememberGraphicsLayer()
    var sharingAchievement by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(sharingAchievement) {
        val achievement = sharingAchievement ?: return@LaunchedEffect
        delay(50)
        val bitmap = runCatching { graphicsLayer.toImageBitmap() }.getOrNull()
        sharingAchievement = null
        if (bitmap != null) {
            AchievementShareHelper.shareAchievementImage(context, bitmap, achievement)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.achievements_title),
                            modifier = Modifier.screenHeading()
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.semantics { contentDescription = navigateBackLabel }
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.achievements_summary, earned.size, achievements.size),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                next?.let { achievement ->
                    Text(
                        stringResource(R.string.achievements_next),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.screenHeading()
                    )
                    AchievementCard(
                        achievement = achievement,
                        highlighted = true,
                        onShare = null
                    )
                }

                if (earned.isNotEmpty()) {
                    Text(
                        stringResource(R.string.achievements_earned),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .screenHeading()
                    )
                    visibleEarned.forEach { achievement ->
                        AchievementCard(
                            achievement = achievement,
                            highlighted = false,
                            onShare = { sharingAchievement = it }
                        )
                    }
                    if (earned.size > EARNED_PREVIEW_COUNT) {
                        SecondaryButton(
                            text = stringResource(
                                if (showAllEarned) R.string.view_less else R.string.view_more
                            ),
                            onClick = { showAllEarned = !showAllEarned }
                        )
                    }
                }

                if (lockedRest.isNotEmpty()) {
                    Text(
                        stringResource(R.string.achievements_locked),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .screenHeading()
                    )
                    visibleLocked.forEach { achievement ->
                        AchievementCard(
                            achievement = achievement,
                            highlighted = false,
                            onShare = null
                        )
                    }
                    if (lockedRest.size > LOCKED_PREVIEW_COUNT) {
                        SecondaryButton(
                            text = stringResource(
                                if (showAllLocked) R.string.view_less else R.string.view_more
                            ),
                            onClick = { showAllLocked = !showAllLocked }
                        )
                    }
                }
            }
        }

        sharingAchievement?.let { achievement ->
            Box(
                modifier = Modifier
                    .size(600.dp, 720.dp)
                    .alpha(0f)
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
            ) {
                AchievementShareCard(
                    achievement = achievement,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    highlighted: Boolean,
    onShare: ((Achievement) -> Unit)?
) {
    val progress = if (achievement.target > 0) {
        (achievement.progress.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val container = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = achievementMetricEmoji(achievement.metric),
                fontSize = 28.sp,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.localizedTitle(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    achievement.localizedDescription(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                if (!achievement.unlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Text(
                        achievementProgressLabel(achievement),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (achievement.unlocked && onShare != null) {
                IconButton(onClick = { onShare(achievement) }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_achievement)
                    )
                }
            } else if (achievement.unlocked) {
                Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
            }
        }
    }
}
