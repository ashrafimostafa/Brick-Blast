package com.example.brickblast.di;

import com.example.brickblast.data.local.BrickBlastDatabase;
import com.example.brickblast.data.local.dao.PlayerWalletDao;
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
public final class DatabaseModule_ProvidePlayerWalletDaoFactory implements Factory<PlayerWalletDao> {
  private final Provider<BrickBlastDatabase> dbProvider;

  public DatabaseModule_ProvidePlayerWalletDaoFactory(Provider<BrickBlastDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlayerWalletDao get() {
    return providePlayerWalletDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePlayerWalletDaoFactory create(
      Provider<BrickBlastDatabase> dbProvider) {
    return new DatabaseModule_ProvidePlayerWalletDaoFactory(dbProvider);
  }

  public static PlayerWalletDao providePlayerWalletDao(BrickBlastDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlayerWalletDao(db));
  }
}
