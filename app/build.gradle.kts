
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.google.gms) // This is critical!
}

android {
    namespace = "com.critetiontech.ctvitalio"
    compileSdk = 35

    sourceSets["main"].resources.srcDir("libs")


    defaultConfig {
        applicationId = "com.critetiontech.ctvitalio"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }

}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

//    implementation(fileTree(mapOf(
//        "dir" to "C:\\Gitea_Projects\\nativevitalio\\app\\libs\\omronconnectivitylibrary.aar",
//        "include" to listOf("*.aar", "*.jar"),
//    )))
    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.aar", "*.jar")
    )))


    /*    implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)*/
    testImplementation(libs.junit)
    implementation(libs.glide)
    implementation("com.squareup.okhttp3:okhttp")
    implementation(libs.shimmer)

    implementation(libs.logging.interceptor) // or latest version
    implementation (libs.androidx.core)
    //Dimen
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)
    implementation(libs.firebase.messaging)
    implementation("com.github.CanHub:Android-Image-Cropper:4.3.2")
//    implementation("com.github.barteksc.pdfviewer:android-pdf-viewer:3.1.0") // Stable version of PDFView


    //otp view
//    implementation (libs.otpview)
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation (libs.android.gif.drawable)
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // retrofit
    implementation(libs.retrofit)
    implementation(libs.lottie.v601)
    implementation(libs.mpandroidchart)

    implementation(libs.converter.gson)
    implementation (libs.lottie)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation (libs.flexbox)
}