package com.example.brickblast.di;

import com.example.brickblast.game.engine.GameEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class GameModule_ProvideGameEngineFactory implements Factory<GameEngine> {
  @Override
  public GameEngine get() {
    return provideGameEngine();
  }

  public static GameModule_ProvideGameEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GameEngine provideGameEngine() {
    return Preconditions.checkNotNullFromProvides(GameModule.INSTANCE.provideGameEngine());
  }

  private static final class InstanceHolder {
    private static final GameModule_ProvideGameEngineFactory INSTANCE = new GameModule_ProvideGameEngineFactory();
  }
}
