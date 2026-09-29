import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.example.aura"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.aura"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    androidResources {
        localeFilters += listOf("en")
    }

    signingConfigs {
        create("release") {
            val storeFileProp = keystoreProperties.getProperty("storeFile") ?: System.getenv("AURA_KEYSTORE_FILE")
            if (storeFileProp != null) {
                val resolvedFile = rootProject.file(storeFileProp)
                if (resolvedFile.exists()) {
                    storeFile = resolvedFile
                    storePassword = keystoreProperties.getProperty("storePassword") ?: System.getenv("AURA_KEYSTORE_PASSWORD")
                    keyAlias = keystoreProperties.getProperty("keyAlias") ?: System.getenv("AURA_KEY_ALIAS")
                    keyPassword = keystoreProperties.getProperty("keyPassword") ?: System.getenv("AURA_KEY_PASSWORD")
                    enableV1Signing = true
                    enableV2Signing = true
                    enableV3Signing = true
                    enableV4Signing = true
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += listOf(
          "/META-INF/{AL2.0,LGPL2.1}",
          "/META-INF/*.version",
          "/META-INF/DEPENDENCIES",
          "/META-INF/LICENSE*",
          "/META-INF/NOTICE*",
          "/META-INF/INDEX.LIST"
        )
      }
    }

    testOptions {
      unitTests {
        isReturnDefaultValues = true
      }
    }
}

ksp {
    // Export Room schema to allow MigrationTestHelper tests to validate every schema change.
    // Commit the schemas/ directory to source control — required before any db version bump.
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}


kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.json:json:20240303")

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Media3
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)
  implementation(libs.media3.ui)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  // Coil
  implementation(libs.coil.compose)

  // DataStore
  implementation(libs.androidx.datastore.preferences)

  // DocumentFile for SAF folder traversal
  implementation("androidx.documentfile:documentfile:1.0.1")

  // Palette for dynamic artwork color extraction
  implementation("androidx.palette:palette-ktx:1.0.0")
}
