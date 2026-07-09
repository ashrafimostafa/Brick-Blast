package com.mostafa.brickblast.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mostafa.brickblast.R
import com.mostafa.brickblast.ui.accessibility.screenHeading
import com.mostafa.brickblast.ui.accessibility.toggleRowSemantics
import com.mostafa.brickblast.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

private data class LanguageOption(val tag: String?, val labelRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val navigateBackLabel = stringResource(R.string.navigate_back)
    val languageOptions = remember {
        listOf(
            LanguageOption(null, R.string.language_system),
            LanguageOption("en", R.string.language_english),
            LanguageOption("fa", R.string.language_persian)
        )
    }
    val currentLanguageLabel = languageOptions.firstOrNull { it.tag == settings.languageTag }
        ?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.language_system)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), modifier = Modifier.screenHeading()) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = navigateBackLabel
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguagePickerRow(
                currentLabel = currentLanguageLabel,
                selectedTag = settings.languageTag,
                options = languageOptions,
                onSelect = { tag ->
                    if (tag != settings.languageTag) {
                        scope.launch {
                            viewModel.applyLanguage(tag)
                            activity?.recreate()
                        }
                    }
                }
            )
            ThemeToggleRow(settings.darkTheme) { viewModel.toggleDarkTheme(it) }
            SettingToggle(
                stringResource(R.string.settings_sound_effects),
                settings.soundEnabled
            ) { viewModel.toggleSound(it) }
            SettingToggle(
                stringResource(R.string.settings_music),
                settings.musicEnabled
            ) { viewModel.toggleMusic(it) }
            SettingToggle(
                stringResource(R.string.settings_vibration),
                settings.vibrationEnabled
            ) { viewModel.toggleVibration(it) }
            SettingToggle(
                stringResource(R.string.settings_trajectory_preview),
                settings.showTrajectory
            ) { viewModel.toggleTrajectory(it) }
            SettingToggle(
                stringResource(R.string.settings_particle_effects),
                settings.particleEffects
            ) { viewModel.toggleParticles(it) }
            SettingToggle(
                stringResource(R.string.settings_achievement_auto_dismiss),
                settings.achievementAutoDismiss
            ) { viewModel.toggleAchievementAutoDismiss(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerRow(
    currentLabel: String,
    selectedTag: String?,
    options: List<LanguageOption>,
    onSelect: (String?) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val languageLabel = stringResource(R.string.settings_language)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showSheet = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            languageLabel,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            currentLabel,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Text(
                text = languageLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            options.forEachIndexed { index, option ->
                val selected = option.tag == selectedTag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSheet = false
                            onSelect(option.tag)
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (index < options.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeToggleRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val label = stringResource(R.string.settings_dark_theme)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .toggleRowSemantics(label, checked)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.semantics { invisibleToUser() }
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .toggleRowSemantics(label, checked)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.semantics { invisibleToUser() }
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}
