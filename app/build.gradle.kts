plugins {
    // firebase
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

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
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // firebase
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))


    // to do: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Firebase Authentication library
    implementation("com.google.firebase:firebase-auth:21.0.5")

    implementation("com.google.firebase:firebase-firestore:24.7.1") // Firestore
    implementation("com.google.firebase:firebase-messaging:23.2.2") // Cloud Messaging

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")

    // Other dependencies (e.g., appcompat, etc.)
    implementation("androidx.appcompat:appcompat:1.4.2")
}

// Apply the Google services plugin
apply(plugin = "com.google.gms.google-services")
