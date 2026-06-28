plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
}
ksp {
    arg("room.generateKotlin", "true")    // Room emite código Kotlin puro
}
android {
    namespace = "com.lab.lab4"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lab.lab4"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // ... dependencias por defecto ...
// Tipografia descargable de Google Fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.8")
// Iconos extendidos de Material (Search, Favorite, etc.)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
// Navegacion entre pantallas
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ── Room (Base de Datos SQLite)
    implementation("androidx.room:room-runtime:2.8.4") // Puedes usar libs.androidx.room.runtime si usas TOML
    implementation("androidx.room:room-ktx:2.8.4")     // Extensiones de Corrutinas y Flow para Room
    ksp("androidx.room:room-compiler:2.8.4")           // Procesador KSP (¡MUY IMPORTANTE: Va con ksp, no con implementation!)

    // ── DataStore (Preferencias Persistentes / Modo Oscuro)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── ViewModel + Lifecycle (Soporte para recolectar estados en Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // ── Google Fused Location Provider & Coroutines Bridge
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0") // Permite usar .await() en tareas de Google

    // ── Accompanist (Control interactivo de permisos en la UI de Compose)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // ── Coil (Carga de imágenes en el perfil de actividad si fuera necesario)
    implementation("io.coil-kt:coil-compose:2.7.0")
}