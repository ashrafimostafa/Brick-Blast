package com.mostafa.brickblast.ui.accessibility

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mostafa.brickblast.R

/** Marks text as a screen/section heading for TalkBack navigation. */
fun Modifier.screenHeading(): Modifier = semantics { heading() }

/** Announces dynamic updates (score, phase) without affecting layout. */
@Composable
fun LiveRegionAnnouncement(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(1.dp)
            .semantics {
                contentDescription = text
                liveRegion = LiveRegionMode.Polite
            }
    )
}

/** Switch row: merges label + on/off state for screen readers. */
fun Modifier.toggleRowSemantics(
    label: String,
    checked: Boolean
): Modifier = composed {
    val onLabel = stringResource(R.string.on)
    val offLabel = stringResource(R.string.off)
    semantics(mergeDescendants = true) {
        role = Role.Switch
        contentDescription = label
        stateDescription = if (checked) onLabel else offLabel
    }
}

/** Respects system "Remove animations" / animator duration scale. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val transition = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            )
            val animator = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            transition == 0f || animator == 0f
        }.getOrDefault(false)
    }
}

fun onOffLabel(checked: Boolean): String = if (checked) "On" else "Off"
