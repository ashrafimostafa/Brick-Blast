package com.example.brickblast.di;

import com.example.brickblast.data.local.BrickBlastDatabase;
import com.example.brickblast.data.local.dao.AchievementDao;
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
public final class DatabaseModule_ProvideAchievementDaoFactory implements Factory<AchievementDao> {
  private final Provider<BrickBlastDatabase> dbProvider;

  public DatabaseModule_ProvideAchievementDaoFactory(Provider<BrickBlastDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AchievementDao get() {
    return provideAchievementDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideAchievementDaoFactory create(
      Provider<BrickBlastDatabase> dbProvider) {
    return new DatabaseModule_ProvideAchievementDaoFactory(dbProvider);
  }

  public static AchievementDao provideAchievementDao(BrickBlastDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAchievementDao(db));
  }
}
