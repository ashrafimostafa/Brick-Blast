package com.mostafa.brickblast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.navigation.compose.rememberNavController
import com.mostafa.brickblast.domain.model.AppSettings
import com.mostafa.brickblast.domain.repository.SettingsRepository
import com.mostafa.brickblast.navigation.BrickBlastNavGraph
import com.mostafa.brickblast.ui.accessibility.rememberReducedMotion
import com.mostafa.brickblast.ui.theme.BrickBlastTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.hypot

/**
 * Shared origin for the theme-change circular reveal. The Settings switch writes
 * the screen position of the toggle here so the animation can emanate from it,
 * mimicking Telegram's day/night switch.
 */
object ThemeRevealController {
    var origin: Offset? = null
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Read the persisted theme synchronously so the app starts in the correct
        // theme with no startup flash or unwanted reveal animation.
        val initialDark = runBlocking { settingsRepository.settings.first().darkTheme }
        setContent {
            val settings by settingsRepository.settings.collectAsState(
                initial = AppSettings(darkTheme = initialDark)
            )
            ThemedAppRoot(targetDark = settings.darkTheme, initialDark = initialDark)
        }
    }
}

@Composable
private fun ThemedAppRoot(targetDark: Boolean, initialDark: Boolean) {
    val reducedMotion = rememberReducedMotion()
    val graphicsLayer = rememberGraphicsLayer()
    var renderedDark by remember { mutableStateOf(initialDark) }
    var oldSnapshot by remember { mutableStateOf<ImageBitmap?>(null) }
    val reveal = remember { Animatable(1f) }
    var revealCenter by remember { mutableStateOf(Offset.Zero) }

    // When the persisted theme changes, snapshot the current (old) frame, switch
    // the live theme, and sweep a circular reveal of the new theme over the old.
    androidx.compose.runtime.LaunchedEffect(targetDark) {
        if (targetDark == renderedDark) return@LaunchedEffect
        oldSnapshot = runCatching { graphicsLayer.toImageBitmap() }.getOrNull()
        revealCenter = ThemeRevealController.origin ?: Offset.Zero
        renderedDark = targetDark
        if (oldSnapshot == null || reducedMotion) {
            oldSnapshot = null
            reveal.snapTo(1f)
            return@LaunchedEffect
        }
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(durationMillis = 480, easing = FastOutSlowInEasing))
        oldSnapshot = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Live app, recorded into a graphics layer so we can snapshot it.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            BrickBlastTheme(darkTheme = renderedDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    BrickBlastNavGraph(navController = navController)
                }
            }
        }

        // Overlay: draw the OLD theme on top, then punch an expanding circular hole
        // from the toggle position to reveal the new theme underneath.
        val snap = oldSnapshot
        if (snap != null) {
            val center = if (revealCenter == Offset.Zero) null else revealCenter
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            ) {
                val c = center ?: Offset(size.width / 2f, size.height * 0.18f)
                val maxRadius = maxOf(
                    hypot(c.x, c.y),
                    hypot(size.width - c.x, c.y),
                    hypot(c.x, size.height - c.y),
                    hypot(size.width - c.x, size.height - c.y)
                )
                drawImage(snap)
                drawCircle(
                    color = Color.Black,
                    radius = maxRadius * reveal.value,
                    center = c,
                    blendMode = BlendMode.Clear
                )
            }
        }
    }
}
