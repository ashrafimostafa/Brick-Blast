package com.example.brickblast.data.repository;

import com.example.brickblast.data.local.dao.GameSaveDao;
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
public final class GameSaveRepositoryImpl_Factory implements Factory<GameSaveRepositoryImpl> {
  private final Provider<GameSaveDao> gameSaveDaoProvider;

  public GameSaveRepositoryImpl_Factory(Provider<GameSaveDao> gameSaveDaoProvider) {
    this.gameSaveDaoProvider = gameSaveDaoProvider;
  }

  @Override
  public GameSaveRepositoryImpl get() {
    return newInstance(gameSaveDaoProvider.get());
  }

  public static GameSaveRepositoryImpl_Factory create(Provider<GameSaveDao> gameSaveDaoProvider) {
    return new GameSaveRepositoryImpl_Factory(gameSaveDaoProvider);
  }

  public static GameSaveRepositoryImpl newInstance(GameSaveDao gameSaveDao) {
    return new GameSaveRepositoryImpl(gameSaveDao);
  }
}
