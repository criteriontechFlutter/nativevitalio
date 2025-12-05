
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.google.gms) // This is critical!
    id("kotlin-kapt")
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
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["appAuthRedirectScheme"] = "com.critetiontech.ctvitalio"


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
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.animation)
    implementation(libs.core)



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
    implementation ("com.google.android.material:material:1.4.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.23")
    //otp view
//    implementation (libs.otpview)
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation (libs.android.gif.drawable)
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // retrofit
    implementation(libs.retrofit)
    implementation(libs.lottie.v601)
    implementation(libs.mpandroidchart)
    implementation(libs.converter.gson)
    implementation (libs.lottie)
    implementation(libs.circleimageview)
    implementation(libs.androidx.cardview)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation (libs.flexbox)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.tbuonomo:dotsindicator:4.3")
    implementation("net.openid:appauth:0.11.1")
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    kapt("androidx.room:room-compiler:2.8.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    

}
