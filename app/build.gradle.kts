import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Versioning Protocol: Auto-increment patch number and code
fun getAutoVersionInfo(): Pair<Int, String> {
    val baseVersion = "1.0"
    val patchFile = file("version.properties")
    val props = Properties()
    if (patchFile.exists()) {
        patchFile.inputStream().use { props.load(it) }
    }
    
    // Starting patch for next build is 5 (so we start at 4 to increment to 5 on first release build)
    var patch = props.getProperty("patch", "4").toInt()
    var code = props.getProperty("code", "4").toInt()
    
    val isReleaseTask = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
    
    if (isReleaseTask) {
        patch++
        code++
        props.setProperty("patch", patch.toString())
        props.setProperty("code", code.toString())
        patchFile.outputStream().use { props.store(it, null) }
    }
    
    return code to "${baseVersion}.${patch}-alpha"
}

android {
    namespace = "com.mohamed.calmplayer"
    compileSdk = 35

    val autoVersion = getAutoVersionInfo()

    defaultConfig {
        applicationId = "com.mohamed.calmplayer"
        minSdk = 24
        targetSdk = 35
        versionCode = autoVersion.first
        versionName = autoVersion.second
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
        }
    }

    buildFeatures {
        compose = true
    }

    // With Kotlin 2.0+, the Compose compiler is a Gradle plugin (applied in the plugins block).

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // Core dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    
    // Compose UI dependencies
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.4")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // Media3 for audio playback
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // DocumentFile
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}