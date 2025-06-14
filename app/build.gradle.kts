plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.petcare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.petcare"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.core)
    implementation (libs.zxing.android.embedded)
    implementation (libs.appcompat.v161)
    implementation (libs.core.ktx)
    implementation (libs.camera.core)
    implementation (libs.camera.camera2)
    implementation (libs.camera.lifecycle)
    implementation (libs.camera.view)

    // ML Kit Barcode Scanning
    implementation (libs.barcode.scanning)
    implementation (libs.volley)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
}