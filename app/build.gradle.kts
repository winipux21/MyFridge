plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "ru.ngtu.myfridge"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.ngtu.myfridge"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Ничего не нужно добавлять, debug signingConfig используется по умолчанию
        }
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
    }
}

dependencies {
    // Базовые зависимости
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM для управления версиями Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Material Icons Extended для дополнительных иконок
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase (без Storage)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // ViewModel + Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ML Kit для распознавания изображений
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:common:18.10.0")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("com.google.android.gms:play-services-mlkit-image-labeling:16.0.8")

    // HTTP-клиент и сериализация
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Тесты
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // JSON
    implementation("org.json:json:20231013")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.compose.material3:material3") // Для TimePicker
}

// Принудительно указываем существующую версию image-labeling-common
configurations.all {
    resolutionStrategy {
        force("com.google.mlkit:image-labeling-common:18.0.0")
    }
}