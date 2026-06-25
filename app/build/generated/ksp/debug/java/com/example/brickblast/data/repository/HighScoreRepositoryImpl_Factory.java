package com.example.brickblast.data.repository;

import com.example.brickblast.data.local.dao.HighScoreDao;
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
public final class HighScoreRepositoryImpl_Factory implements Factory<HighScoreRepositoryImpl> {
  private final Provider<HighScoreDao> highScoreDaoProvider;

  public HighScoreRepositoryImpl_Factory(Provider<HighScoreDao> highScoreDaoProvider) {
    this.highScoreDaoProvider = highScoreDaoProvider;
  }

  @Override
  public HighScoreRepositoryImpl get() {
    return newInstance(highScoreDaoProvider.get());
  }

  public static HighScoreRepositoryImpl_Factory create(
      Provider<HighScoreDao> highScoreDaoProvider) {
    return new HighScoreRepositoryImpl_Factory(highScoreDaoProvider);
  }

  public static HighScoreRepositoryImpl newInstance(HighScoreDao highScoreDao) {
    return new HighScoreRepositoryImpl(highScoreDao);
  }
}
