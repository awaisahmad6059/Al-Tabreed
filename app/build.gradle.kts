plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.aak.al_tabreed"
    compileSdk = 36

    defaultConfig {

        applicationId = "com.aak.al_tabreed"
        minSdk = 23
        targetSdk = 36
        versionCode = 12
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resConfigs("en", "ar")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false // ✅ Kotlin DSL property
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"

            )
        }
    }
    bundle {
        language {
            // Disable language split to keep all resources in one bundle
            enableSplit = false
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.material3.jvmstubs)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// or latest version

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    implementation("id.zelory:compressor:3.0.1")
// for image compression
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.squareup.picasso:picasso:2.8")

    implementation("com.facebook.soloader:soloader:0.10.4")
}