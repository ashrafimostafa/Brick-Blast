package com.example.brickblast.ui.viewmodel;

import com.example.brickblast.domain.repository.HighScoreRepository;
import com.example.brickblast.domain.repository.PlayerRepository;
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
public final class StatisticsViewModel_Factory implements Factory<StatisticsViewModel> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  private final Provider<HighScoreRepository> highScoreRepositoryProvider;

  public StatisticsViewModel_Factory(Provider<PlayerRepository> playerRepositoryProvider,
      Provider<HighScoreRepository> highScoreRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
    this.highScoreRepositoryProvider = highScoreRepositoryProvider;
  }

  @Override
  public StatisticsViewModel get() {
    return newInstance(playerRepositoryProvider.get(), highScoreRepositoryProvider.get());
  }

  public static StatisticsViewModel_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider,
      Provider<HighScoreRepository> highScoreRepositoryProvider) {
    return new StatisticsViewModel_Factory(playerRepositoryProvider, highScoreRepositoryProvider);
  }

  public static StatisticsViewModel newInstance(PlayerRepository playerRepository,
      HighScoreRepository highScoreRepository) {
    return new StatisticsViewModel(playerRepository, highScoreRepository);
  }
}
