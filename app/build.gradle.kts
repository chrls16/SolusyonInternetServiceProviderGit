plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.solusyoninternetserviceprovider"
    compileSdk = 36 // Note: Simplified this line to standard format, but your block was fine too!

    defaultConfig {
        applicationId = "com.example.solusyoninternetserviceprovider"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- FIREBASE DEPENDENCIES ---

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))

    // Add the dependency for the Firebase Authentication library
    implementation("com.google.firebase:firebase-auth")

    // Add the dependency for the Firebase Authentication library
    implementation("com.google.firebase:firebase-auth")

    // Add the dependency for the Realtime Database library
    implementation("com.google.firebase:firebase-database")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.firebase:firebase-storage")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.android.gms:play-services-maps:19.0.0")

    implementation("org.osmdroid:osmdroid-android:6.1.18")

    implementation("com.google.firebase:firebase-messaging")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}