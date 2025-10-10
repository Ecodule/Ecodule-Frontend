import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization) // Version Catalog の alias を使用
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    // id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"  ← 削除
}

val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.ecodule"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ecodule"
        minSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    flavorDimensions("env")

    val baseUrl = project.findProperty("BASE_URL") as? String ?: "https://ecodule.ddns.net"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String","BASE_URL", "\"$baseUrl\"")
            buildConfigField(
                "String",
                "GOOGLE_WEB_CLIENT_ID",
                "${properties.getProperty("GOOGLE_WEB_CLIENT_ID")}"
            )
        }
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String","BASE_URL", "\"$baseUrl\"")
            buildConfigField(
                "String",
                "GOOGLE_WEB_CLIENT_ID",
                "${properties.getProperty("GOOGLE_WEB_CLIENT_ID")}"
            )
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String","BASE_URL", "\"$baseUrl\"")
            buildConfigField(
                "String",
                "GOOGLE_WEB_CLIENT_ID",
                "${properties.getProperty("GOOGLE_WEB_CLIENT_ID")}"
            )
        }
    }
}

dependencies {
//    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation(libs.androidx.compose.bom)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended.android)
    implementation(libs.androidx.material3.android)

    /*
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation") // 追加（AnimatedContent等明示）
    */

    // google oauth
    implementation(libs.googleid)
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.accompanist.swiperefresh)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)

    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.appwidget)
    // DataStore
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    // serializer
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation("androidx.navigation:navigation-compose:2.9.5")


}