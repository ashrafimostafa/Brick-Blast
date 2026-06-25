package com.example.brickblast.ui.viewmodel;

import com.example.brickblast.domain.repository.SettingsRepository;
import com.example.brickblast.game.audio.AudioManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<AudioManager> audioManagerProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AudioManager> audioManagerProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.audioManagerProvider = audioManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), audioManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AudioManager> audioManagerProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, audioManagerProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      AudioManager audioManager) {
    return new SettingsViewModel(settingsRepository, audioManager);
  }
}
