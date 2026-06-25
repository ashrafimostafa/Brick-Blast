package com.example.brickblast.di;

import com.example.brickblast.data.local.BrickBlastDatabase;
import com.example.brickblast.data.local.dao.PlayerUpgradesDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvidePlayerUpgradesDaoFactory implements Factory<PlayerUpgradesDao> {
  private final Provider<BrickBlastDatabase> dbProvider;

  public DatabaseModule_ProvidePlayerUpgradesDaoFactory(Provider<BrickBlastDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlayerUpgradesDao get() {
    return providePlayerUpgradesDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePlayerUpgradesDaoFactory create(
      Provider<BrickBlastDatabase> dbProvider) {
    return new DatabaseModule_ProvidePlayerUpgradesDaoFactory(dbProvider);
  }

  public static PlayerUpgradesDao providePlayerUpgradesDao(BrickBlastDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlayerUpgradesDao(db));
  }
}
