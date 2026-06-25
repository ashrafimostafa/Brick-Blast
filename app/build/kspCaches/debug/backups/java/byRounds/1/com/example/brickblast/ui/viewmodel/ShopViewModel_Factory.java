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
public final class ShopViewModel_Factory implements Factory<ShopViewModel> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  public ShopViewModel_Factory(Provider<PlayerRepository> playerRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public ShopViewModel get() {
    return newInstance(playerRepositoryProvider.get());
  }

  public static ShopViewModel_Factory create(Provider<PlayerRepository> playerRepositoryProvider) {
    return new ShopViewModel_Factory(playerRepositoryProvider);
  }

  public static ShopViewModel newInstance(PlayerRepository playerRepository) {
    return new ShopViewModel(playerRepository);
  }
}
