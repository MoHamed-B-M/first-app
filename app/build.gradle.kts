plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.mohamed.calmplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mohamed.calmplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    // With Kotlin 2.0+, the Compose compiler is a Gradle plugin (applied in the plugins block).

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    
    // Compose UI dependencies
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Material Design 3
    implementation("androidx.compose.material3:material3:1.5.0-alpha13")
    implementation("androidx.compose.material3.adaptive:adaptive:1.3.0-alpha07")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0-alpha07")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.3.0-alpha07")
    
    // Graphics Shapes for morphing
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    
    // Material Icons
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    
    // For ContextCompat
    implementation("androidx.core:core:1.12.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Media3 dependencies for Audio Player
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-session:1.2.0")
    
    // DataStore for settings
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Debug dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}