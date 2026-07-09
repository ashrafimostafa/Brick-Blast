package com.mostafa.brickblast.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.mostafa.brickblast.R
import com.mostafa.brickblast.domain.model.Achievement
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Date

object AchievementShareHelper {

    fun shareAchievementImage(
        context: Context,
        bitmap: ImageBitmap,
        achievement: Achievement
    ) {
        val cacheDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(cacheDir, "brick_blast_achievement_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val title = achievement.localizedTitle(context)
        val description = achievement.localizedDescription(context)
        val shareText = context.getString(R.string.share_achievement_text, title, description)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)))
    }
}

@Composable
fun AchievementShareCard(
    achievement: Achievement,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B4332)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8F5E9)))
    }
    val ink = if (isDark) Color.White else Color(0xFF101418)
    val accent = Color(0xFFFFD600)
    val title = achievement.localizedTitle()
    val description = achievement.localizedDescription()
    val unlockedLabel = stringResource(R.string.achievement_unlocked)
    val dateLabel = achievement.unlockedAt?.let {
        stringResource(
            R.string.achievement_unlocked_on,
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it))
        )
    }

    Box(
        modifier = modifier
            .size(600.dp, 720.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = achievementMetricEmoji(achievement.metric),
                fontSize = 72.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = unlockedLabel,
                color = accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = ink,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                color = ink.copy(alpha = 0.8f),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            dateLabel?.let {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = it,
                    color = ink.copy(alpha = 0.65f),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.title_brick_blast),
                color = accent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun achievementProgressLabel(achievement: Achievement): String = when (achievement.metric) {
    com.mostafa.brickblast.domain.model.AchievementMetric.PLAY_TIME -> {
        stringResource(
            R.string.achievement_progress_playtime,
            formatPlayTimeShort(achievement.progress.coerceAtMost(achievement.target)),
            formatPlayTimeShort(achievement.target)
        )
    }
    else -> stringResource(
        R.string.achievement_progress,
        formatAchievementCount(achievement.progress.coerceAtMost(achievement.target)),
        formatAchievementCount(achievement.target)
    )
}
