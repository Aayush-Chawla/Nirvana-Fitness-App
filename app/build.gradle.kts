plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase plugin
//    id("org.jetbrains.kotlin.android") // Kotlin plugin (if you're using Kotlin)
}

android {
    namespace = "com.example.nirvana"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nirvana"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read Firebase config from local.properties
        buildConfigField("String", "FIREBASE_API_KEY", "\"${project.properties["FIREBASE_API_KEY"]}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${project.properties["FIREBASE_APP_ID"]}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${project.properties["FIREBASE_PROJECT_ID"]}\"")
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"${project.properties["FIREBASE_DATABASE_URL"]}\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"${project.properties["FIREBASE_STORAGE_BUCKET"]}\"")
        buildConfigField("String", "FIREBASE_SENDER_ID", "\"${project.properties["FIREBASE_SENDER_ID"]}\"")
    }

    buildTypes {
//        debug {
//            isDebuggable = true
//            isMinifyEnabled = false
//        }
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

    // Enable BuildConfig generation
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

}

dependencies {
    // AndroidX libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")

    // Firebase BoM and dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.6.0")
    implementation("androidx.navigation:navigation-ui:2.6.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")

    // Network dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Scribe Java OAuth library
    implementation("com.github.scribejava:scribejava-core:8.3.3")
    implementation("com.github.scribejava:scribejava-apis:8.3.3")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // MPAndroidChart for charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}