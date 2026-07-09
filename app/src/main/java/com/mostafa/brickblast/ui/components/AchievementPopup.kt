package com.mostafa.brickblast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.ui.accessibility.LiveRegionAnnouncement
import com.mostafa.brickblast.ui.accessibility.rememberReducedMotion
import com.mostafa.brickblast.ui.util.achievementMetricEmoji
import com.mostafa.brickblast.ui.util.localizedDescription
import com.mostafa.brickblast.ui.util.localizedTitle
import kotlinx.coroutines.delay

private const val AUTO_DISMISS_MS = 4_000L

@Composable
fun AchievementPopup(
    achievements: List<Achievement>,
    onDismiss: () -> Unit,
    autoDismissEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val achievement = achievements.firstOrNull() ?: return
    val reducedMotion = rememberReducedMotion()
    val title = achievement.localizedTitle()
    val description = achievement.localizedDescription()
    val announcement = stringResource(
        R.string.achievement_unlocked_announcement,
        title,
        description
    )
    val closeLabel = stringResource(R.string.close)
    val continueLabel = stringResource(R.string.continue_playing)

    LaunchedEffect(achievement.id, autoDismissEnabled) {
        if (!autoDismissEnabled) return@LaunchedEffect
        delay(AUTO_DISMISS_MS)
        onDismiss()
    }

    LiveRegionAnnouncement(text = announcement)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        AnimatedVisibility(
            visible = true,
            enter = if (reducedMotion) {
                fadeIn(tween(0))
            } else {
                fadeIn(tween(350)) +
                    slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = tween(450, easing = FastOutSlowInEasing)
                    ) +
                    scaleIn(
                        initialScale = 0.7f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
            },
            exit = if (reducedMotion) fadeOut(tween(0)) else fadeOut(tween(300)),
            modifier = Modifier.padding(horizontal = 28.dp)
        ) {
            val pulse = if (reducedMotion) {
                1f
            } else {
                val transition = rememberInfiniteTransition(label = "achievementPulse")
                val scale by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "achievementPulseScale"
                )
                scale
            }

            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B5E20), Color(0xFF0D3B1A))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = announcement
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = achievementMetricEmoji(achievement.metric),
                        fontSize = 48.sp,
                        modifier = Modifier.scale(pulse)
                    )
                    Text(
                        stringResource(R.string.achievement_unlocked),
                        color = Color(0xFFFFD600),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    if (autoDismissEnabled) {
                        Text(
                            stringResource(R.string.achievement_auto_close_hint),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    GameButton(
                        text = continueLabel,
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .semantics { contentDescription = closeLabel }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
