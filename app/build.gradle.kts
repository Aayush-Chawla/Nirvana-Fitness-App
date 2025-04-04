plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase plugin
//    id("org.jetbrains.kotlin.android") // Kotlin plugin (if you're using Kotlin)
}

android {
    namespace = "com.example.nirvana"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nirvana"
        minSdk = 26
        targetSdk = 35
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
    implementation(libs.appcompat.v161)
    implementation(libs.material.v190)
    implementation(libs.constraintlayout.v214)
    implementation(libs.activity.ktx) // Use -ktx for Kotlin extensions

    // Firebase BoM (Bill of Materials)
    implementation(platform(libs.firebase.bom))

    // Firebase dependencies (no version needed, as BoM manages them)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)     // Firebase Authentication
    implementation(libs.google.firebase.firestore)
    implementation(libs.firebase.messaging)

    // Navigation Component
    implementation(libs.navigation.fragment.ktx)  // or the latest version
    implementation(libs.navigation.ui.ktx)        // or the latest version

//    implementation(libs.navigation.fragment)
//    implementation(libs.navigation.ui)

    implementation(libs.activity)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.database)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)

    // Google Sign-In
    implementation(libs.play.services.auth)


//    ----- Retrofit and OkHttp dependencies -----

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Converter for JSON parsing
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Gson for JSON serialization/deserialization
    implementation("com.google.code.gson:gson:2.10.1")

    // For OAuth signing (required by FatSecret API)
    implementation("oauth.signpost:signpost-core:1.2.1.2")
    implementation("oauth.signpost:signpost-commonshttp4:1.2.1.2")


    // Replace Signpost with these
    implementation("com.github.scribejava:scribejava-core:8.3.3")
    implementation("com.github.scribejava:scribejava-httpclient-okhttp:8.3.3")


    // Material Design
    implementation(libs.material.v1110)

    // Chart library (for displaying macros)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")




}