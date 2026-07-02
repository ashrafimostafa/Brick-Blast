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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.mostafa.brickblast.R
import java.io.File
import java.io.FileOutputStream

object ScoreShareHelper {

    fun shareScoreImage(
        context: Context,
        bitmap: ImageBitmap,
        score: Int,
        round: Int,
        mode: String
    ) {
        val cacheDir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(cacheDir, "brick_blast_score_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val localizedMode = gameModeLabel(context, mode)
        val shareText = context.getString(R.string.share_score_text, score, round, localizedMode)
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
fun ScoreShareCard(
    score: Int,
    round: Int,
    mode: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEEF3F8)))
    }
    val ink = if (isDark) Color.White else Color(0xFF101418)
    val accent = Color(0xFF448AFF)
    val localizedMode = gameModeLabel(mode)

    Box(
        modifier = modifier
            .size(600.dp, 800.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.title_brick_blast),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.share_card_game_over),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF5252)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = score.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = ink
            )
            Text(
                text = stringResource(R.string.share_card_points),
                fontSize = 16.sp,
                color = ink.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.share_card_round, round),
                fontSize = 20.sp,
                color = ink.copy(alpha = 0.85f)
            )
            Text(
                text = localizedMode,
                fontSize = 16.sp,
                color = ink.copy(alpha = 0.55f)
            )
            Spacer(Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.share_card_challenge),
                fontSize = 14.sp,
                color = accent
            )
        }
    }
}
