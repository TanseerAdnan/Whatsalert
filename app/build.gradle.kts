plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.whatsalert"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.whatsalert"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("D://whatsalert key/whatsalert.jks")
            storePassword = "Tanseer123"
            keyAlias = "Key0"
            keyPassword = "Tanseer123"

            enableV2Signing = true
            enableV3Signing = true
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                signingConfig = signingConfigs.getByName("release")
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        buildFeatures {
            viewBinding = true
            buildConfig = true
        }
    }

    dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.navigation.fragment)
        implementation(libs.fragment)
        implementation(libs.navigation.ui)
        implementation(libs.work.runtime)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }
}