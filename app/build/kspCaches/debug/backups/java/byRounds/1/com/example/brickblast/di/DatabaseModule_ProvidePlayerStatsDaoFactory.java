package com.example.brickblast.di;

import com.example.brickblast.data.local.BrickBlastDatabase;
import com.example.brickblast.data.local.dao.PlayerStatsDao;
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
public final class DatabaseModule_ProvidePlayerStatsDaoFactory implements Factory<PlayerStatsDao> {
  private final Provider<BrickBlastDatabase> dbProvider;

  public DatabaseModule_ProvidePlayerStatsDaoFactory(Provider<BrickBlastDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlayerStatsDao get() {
    return providePlayerStatsDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePlayerStatsDaoFactory create(
      Provider<BrickBlastDatabase> dbProvider) {
    return new DatabaseModule_ProvidePlayerStatsDaoFactory(dbProvider);
  }

  public static PlayerStatsDao providePlayerStatsDao(BrickBlastDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlayerStatsDao(db));
  }
}
