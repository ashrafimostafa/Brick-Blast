package com.mostafa.brickblast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.domain.model.Achievement
import com.mostafa.brickblast.ui.accessibility.LiveRegionAnnouncement
import com.mostafa.brickblast.ui.accessibility.rememberReducedMotion
import kotlinx.coroutines.delay

@Composable
fun AchievementPopup(
    achievements: List<Achievement>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val achievement = achievements.firstOrNull() ?: return
    val reducedMotion = rememberReducedMotion()
    val announcement = "Achievement unlocked: ${achievement.title}. ${achievement.description}"

    LaunchedEffect(achievement.id) {
        delay(3000)
        onDismiss()
    }

    LiveRegionAnnouncement(text = announcement)

    AnimatedVisibility(
        visible = true,
        enter = if (reducedMotion) fadeIn(tween(0)) else fadeIn(tween(400)) + scaleIn(tween(400)),
        exit = if (reducedMotion) fadeOut(tween(0)) else fadeOut(tween(300)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFF1B5E20).copy(0.9f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = announcement
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Achievement Unlocked!", color = Color(0xFFFFD600), fontSize = 14.sp)
            Text(achievement.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(achievement.description, color = Color.White.copy(0.8f), fontSize = 13.sp)
        }
    }
}
