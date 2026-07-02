package com.mostafa.brickblast.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.R
import com.mostafa.brickblast.ui.accessibility.screenHeading
import com.mostafa.brickblast.ui.components.GameButton
import com.mostafa.brickblast.ui.components.SecondaryButton

@Composable
fun ContinueOfferOverlay(
    loading: Boolean,
    onWatchAd: () -> Unit,
    onGiveUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.continue_so_close),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF5252),
            modifier = Modifier.screenHeading()
        )
        Text(
            text = stringResource(R.string.continue_ad_message),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        GameButton(
            text = if (loading) {
                stringResource(R.string.continue_loading_ad)
            } else {
                stringResource(R.string.continue_watch_ad)
            },
            onClick = onWatchAd,
            enabled = !loading
        )
        SecondaryButton(
            text = stringResource(R.string.continue_end_game),
            onClick = onGiveUp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
