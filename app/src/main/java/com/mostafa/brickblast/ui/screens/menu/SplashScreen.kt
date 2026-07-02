package com.mostafa.brickblast.ui.screens.menu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mostafa.brickblast.R
import com.mostafa.brickblast.ui.accessibility.rememberReducedMotion
import com.mostafa.brickblast.ui.accessibility.screenHeading
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    val alpha = remember { Animatable(0f) }
    val ink = MaterialTheme.colorScheme.onBackground
    val accent = MaterialTheme.colorScheme.primary

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            alpha.snapTo(1f)
            delay(400)
            onFinished()
        } else {
            alpha.animateTo(1f, animationSpec = tween(800))
            delay(1500)
            alpha.animateTo(0f, animationSpec = tween(500))
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.alpha(alpha.value)
        ) {
            Text(
                text = stringResource(R.string.title_brick_blast),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.screenHeading()
            )
            Text(
                text = stringResource(R.string.tagline_break_bounce_blast),
                fontSize = 16.sp,
                color = ink.copy(alpha = 0.7f)
            )
        }
    }
}
