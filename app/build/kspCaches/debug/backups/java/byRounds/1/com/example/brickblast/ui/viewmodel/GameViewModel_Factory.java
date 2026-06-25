package com.example.brickblast.ui.viewmodel;

import com.example.brickblast.domain.repository.GameSaveRepository;
import com.example.brickblast.domain.repository.HighScoreRepository;
import com.example.brickblast.domain.repository.PlayerRepository;
import com.example.brickblast.domain.repository.SettingsRepository;
import com.example.brickblast.game.audio.AudioManager;
import com.example.brickblast.game.engine.GameEngine;
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
public final class GameViewModel_Factory implements Factory<GameViewModel> {
  private final Provider<GameEngine> engineProvider;

  private final Provider<PlayerRepository> playerRepositoryProvider;

  private final Provider<GameSaveRepository> gameSaveRepositoryProvider;

  private final Provider<HighScoreRepository> highScoreRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<AudioManager> audioManagerProvider;

  public GameViewModel_Factory(Provider<GameEngine> engineProvider,
      Provider<PlayerRepository> playerRepositoryProvider,
      Provider<GameSaveRepository> gameSaveRepositoryProvider,
      Provider<HighScoreRepository> highScoreRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AudioManager> audioManagerProvider) {
    this.engineProvider = engineProvider;
    this.playerRepositoryProvider = playerRepositoryProvider;
    this.gameSaveRepositoryProvider = gameSaveRepositoryProvider;
    this.highScoreRepositoryProvider = highScoreRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.audioManagerProvider = audioManagerProvider;
  }

  @Override
  public GameViewModel get() {
    return newInstance(engineProvider.get(), playerRepositoryProvider.get(), gameSaveRepositoryProvider.get(), highScoreRepositoryProvider.get(), settingsRepositoryProvider.get(), audioManagerProvider.get());
  }

  public static GameViewModel_Factory create(Provider<GameEngine> engineProvider,
      Provider<PlayerRepository> playerRepositoryProvider,
      Provider<GameSaveRepository> gameSaveRepositoryProvider,
      Provider<HighScoreRepository> highScoreRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<AudioManager> audioManagerProvider) {
    return new GameViewModel_Factory(engineProvider, playerRepositoryProvider, gameSaveRepositoryProvider, highScoreRepositoryProvider, settingsRepositoryProvider, audioManagerProvider);
  }

  public static GameViewModel newInstance(GameEngine engine, PlayerRepository playerRepository,
      GameSaveRepository gameSaveRepository, HighScoreRepository highScoreRepository,
      SettingsRepository settingsRepository, AudioManager audioManager) {
    return new GameViewModel(engine, playerRepository, gameSaveRepository, highScoreRepository, settingsRepository, audioManager);
  }
}
