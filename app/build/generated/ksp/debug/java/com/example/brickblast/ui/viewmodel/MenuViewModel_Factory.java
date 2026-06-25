package com.example.brickblast.ui.viewmodel;

import com.example.brickblast.domain.repository.GameSaveRepository;
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
public final class MenuViewModel_Factory implements Factory<MenuViewModel> {
  private final Provider<GameSaveRepository> gameSaveRepositoryProvider;

  private final Provider<PlayerRepository> playerRepositoryProvider;

  public MenuViewModel_Factory(Provider<GameSaveRepository> gameSaveRepositoryProvider,
      Provider<PlayerRepository> playerRepositoryProvider) {
    this.gameSaveRepositoryProvider = gameSaveRepositoryProvider;
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public MenuViewModel get() {
    return newInstance(gameSaveRepositoryProvider.get(), playerRepositoryProvider.get());
  }

  public static MenuViewModel_Factory create(
      Provider<GameSaveRepository> gameSaveRepositoryProvider,
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new MenuViewModel_Factory(gameSaveRepositoryProvider, playerRepositoryProvider);
  }

  public static MenuViewModel newInstance(GameSaveRepository gameSaveRepository,
      PlayerRepository playerRepository) {
    return new MenuViewModel(gameSaveRepository, playerRepository);
  }
}
