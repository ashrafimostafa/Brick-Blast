package com.example.brickblast.game.audio;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AudioManager_Factory implements Factory<AudioManager> {
  private final Provider<Context> contextProvider;

  public AudioManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AudioManager get() {
    return newInstance(contextProvider.get());
  }

  public static AudioManager_Factory create(Provider<Context> contextProvider) {
    return new AudioManager_Factory(contextProvider);
  }

  public static AudioManager newInstance(Context context) {
    return new AudioManager(context);
  }
}
