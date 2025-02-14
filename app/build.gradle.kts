plugins {
    id("com.android.application")
//    id("com.google.gms.google-services") // Firebase plugin
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
    }
}

dependencies {
    // AndroidX libraries
    implementation(libs.appcompat.v161)
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.10.0") // Use -ktx for Kotlin extensions

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Firebase dependencies (no version needed, as BoM manages them)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}