import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// Release signing is configured from a local keystore.properties file (for local
// release builds) or from environment variables (for CI). Neither the keystore
// nor its passwords are committed to the repository.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties().apply {
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

// Reads a config value from local.properties first (local dev), then falls back
// to an environment variable of the same name (CI / GitHub Actions secrets).
fun localProp(key: String, default: String = ""): String =
    localProperties.getProperty(key) ?: System.getenv(key) ?: default

fun signingValue(propKey: String, envKey: String): String? =
    (keystoreProperties[propKey] as String?) ?: System.getenv(envKey)

val releaseStoreFilePath = signingValue("storeFile", "KEYSTORE_FILE")
val hasReleaseSigning = releaseStoreFilePath != null

android {
    namespace = "com.mostafa.brickblast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mostafa.brickblast"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("fdroid") {
            dimension = "distribution"
            buildConfigField("boolean", "ADS_ENABLED", "false")
            buildConfigField("String", "TAPSELL_APP_KEY", "\"\"")
            buildConfigField("String", "TAPSELL_REWARDED_ZONE_ID", "\"\"")
        }
        create("store") {
            dimension = "distribution"
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField(
                "String",
                "TAPSELL_APP_KEY",
                "\"${localProp("TAPSELL_APP_KEY")}\""
            )
            buildConfigField(
                "String",
                "TAPSELL_REWARDED_ZONE_ID",
                "\"${localProp("TAPSELL_REWARDED_ZONE_ID")}\""
            )
            proguardFile("src/store/proguard-rules-store.pro")
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the release keystore when available (local or CI); otherwise the
            // build still completes unsigned so contributors without the key can build.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                null
            }
        }
    }

    // F-Droid / reproducible builds: strip the Google-signed dependency metadata
    // block that gets embedded in the APK/bundle (proprietary, breaks repro builds).
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Store flavor only: Tapsell Plus (rewarded video). Not included in fdroid builds.
    "storeImplementation"("ir.tapsell.plus:tapsell-plus-sdk-android:2.3.2")
}
