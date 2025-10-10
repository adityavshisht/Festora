plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.metabots.festora"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.metabots.festora"
        minSdk = 24
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Material Components (latest stable)
    implementation("com.google.android.material:material:1.12.0")


    // Firebase BOM (handles versions)
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // Firebase Auth (for Google + Email login)
    implementation("com.google.firebase:firebase-auth:22.3.1    ")

    implementation ("com.google.firebase:firebase-firestore")

    // (optional) Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}