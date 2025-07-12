plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Necesario para el procesador de anotaciones de Roo
}

android {
    namespace = "com.example.apprm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.apprm"
        minSdk = 29
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel y LiveData (Esenciales para MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Retrofit y convertidor para Kotlinx.serialization
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Interceptor de logging para OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // UI (RecyclerView, CardView, Material, Glide)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Constraints Layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // Para Kotlin, usa kapt en lugar de annotationProcessor
    kapt("androidx.room:room-compiler:2.6.1")
    // Soporte para Corrutinas de Room (opcional pero muy recomendado para operaciones asíncronas)
    implementation("androidx.room:room-ktx:2.6.1")

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    // Para geocodificación (opcional, si quieres convertir nombres a coordenadas)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}