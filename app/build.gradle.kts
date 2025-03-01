plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.homestorage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.homestorage"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "1.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/src/androidTest/schemas")
        }
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {
    // 基础依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM 及基础 Compose 依赖
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)

    // Navigation Compose（导航）
    implementation(libs.androidx.navigation.compose)

    // Room 数据库相关依赖
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.room.testing)
    add("kapt", libs.androidx.room.compiler)

    // Lifecycle & ViewModel 与 Compose 结合
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Gson 用于 JSON 序列化（导入/导出数据）
    implementation(libs.gson)

    // Coil 用于图片加载
    implementation(libs.coil.compose)

    // 图标库
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.material.icons.extended.v177)
    implementation(libs.androidx.material)

    implementation(libs.ucrop.v226)
    implementation(libs.text.recognition.chinese)

    implementation(libs.google.accompanist.systemuicontroller)
    implementation(libs.google.accompanist.pager)

    implementation(libs.jetbrains.kotlinx.serialization.json)

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
