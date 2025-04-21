// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Root build.gradle.kts (Project-level)
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.google.gms) apply false
}
