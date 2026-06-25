package com.example.brickblast.data.repository;

import com.example.brickblast.data.local.dao.AchievementDao;
import com.example.brickblast.data.local.dao.PlayerStatsDao;
import com.example.brickblast.data.local.dao.PlayerUpgradesDao;
import com.example.brickblast.data.local.dao.PlayerWalletDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PlayerRepositoryImpl_Factory implements Factory<PlayerRepositoryImpl> {
  private final Provider<PlayerWalletDao> walletDaoProvider;

  private final Provider<PlayerUpgradesDao> upgradesDaoProvider;

  private final Provider<PlayerStatsDao> statsDaoProvider;

  private final Provider<AchievementDao> achievementDaoProvider;

  public PlayerRepositoryImpl_Factory(Provider<PlayerWalletDao> walletDaoProvider,
      Provider<PlayerUpgradesDao> upgradesDaoProvider, Provider<PlayerStatsDao> statsDaoProvider,
      Provider<AchievementDao> achievementDaoProvider) {
    this.walletDaoProvider = walletDaoProvider;
    this.upgradesDaoProvider = upgradesDaoProvider;
    this.statsDaoProvider = statsDaoProvider;
    this.achievementDaoProvider = achievementDaoProvider;
  }

  @Override
  public PlayerRepositoryImpl get() {
    return newInstance(walletDaoProvider.get(), upgradesDaoProvider.get(), statsDaoProvider.get(), achievementDaoProvider.get());
  }

  public static PlayerRepositoryImpl_Factory create(Provider<PlayerWalletDao> walletDaoProvider,
      Provider<PlayerUpgradesDao> upgradesDaoProvider, Provider<PlayerStatsDao> statsDaoProvider,
      Provider<AchievementDao> achievementDaoProvider) {
    return new PlayerRepositoryImpl_Factory(walletDaoProvider, upgradesDaoProvider, statsDaoProvider, achievementDaoProvider);
  }

  public static PlayerRepositoryImpl newInstance(PlayerWalletDao walletDao,
      PlayerUpgradesDao upgradesDao, PlayerStatsDao statsDao, AchievementDao achievementDao) {
    return new PlayerRepositoryImpl(walletDao, upgradesDao, statsDao, achievementDao);
  }
}
