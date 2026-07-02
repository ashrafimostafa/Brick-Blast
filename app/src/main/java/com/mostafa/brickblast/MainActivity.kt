package com.mostafa.brickblast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mostafa.brickblast.domain.model.AppSettings
import com.mostafa.brickblast.domain.repository.SettingsRepository
import com.mostafa.brickblast.navigation.BrickBlastNavGraph
import com.mostafa.brickblast.ui.theme.BrickBlastTheme
import com.mostafa.brickblast.ui.util.isPersianLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialDark = runBlocking { settingsRepository.settings.first().darkTheme }
        setContent {
            val settings by settingsRepository.settings.collectAsState(
                initial = AppSettings(darkTheme = initialDark)
            )
            val persianUi = isPersianLocale(settings.languageTag)
            ThemedAppRoot(
                targetDark = settings.darkTheme,
                initialDark = initialDark,
                persianUi = persianUi
            )
        }
    }
}

@Composable
private fun ThemedAppRoot(targetDark: Boolean, initialDark: Boolean, persianUi: Boolean) {
    var renderedDark by remember { mutableStateOf(initialDark) }
    LaunchedEffect(targetDark) {
        if (targetDark != renderedDark) {
            renderedDark = targetDark
        }
    }
    BrickBlastTheme(darkTheme = renderedDark, persianUi = persianUi) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            BrickBlastNavGraph(navController = navController)
        }
    }
}
