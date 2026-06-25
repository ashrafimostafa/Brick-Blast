package com.example.brickblast.di;

import com.example.brickblast.data.local.BrickBlastDatabase;
import com.example.brickblast.data.local.dao.GameSaveDao;
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
public final class DatabaseModule_ProvideGameSaveDaoFactory implements Factory<GameSaveDao> {
  private final Provider<BrickBlastDatabase> dbProvider;

  public DatabaseModule_ProvideGameSaveDaoFactory(Provider<BrickBlastDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public GameSaveDao get() {
    return provideGameSaveDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideGameSaveDaoFactory create(
      Provider<BrickBlastDatabase> dbProvider) {
    return new DatabaseModule_ProvideGameSaveDaoFactory(dbProvider);
  }

  public static GameSaveDao provideGameSaveDao(BrickBlastDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideGameSaveDao(db));
  }
}
