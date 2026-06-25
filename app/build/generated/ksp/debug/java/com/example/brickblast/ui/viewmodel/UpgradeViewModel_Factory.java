package com.example.brickblast.ui.viewmodel;

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
public final class UpgradeViewModel_Factory implements Factory<UpgradeViewModel> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  public UpgradeViewModel_Factory(Provider<PlayerRepository> playerRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public UpgradeViewModel get() {
    return newInstance(playerRepositoryProvider.get());
  }

  public static UpgradeViewModel_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new UpgradeViewModel_Factory(playerRepositoryProvider);
  }

  public static UpgradeViewModel newInstance(PlayerRepository playerRepository) {
    return new UpgradeViewModel(playerRepository);
  }
}
