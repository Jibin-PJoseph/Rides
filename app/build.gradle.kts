plugins {
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safe.args)

}

android {
    namespace = "com.ibm.rides"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ibm.rides"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //viewmodel and Flow
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    //Dagger-Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    //viewpager2
    implementation(libs.viewpager2)


    //Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutinesTest)
    testImplementation(libs.coreTesting)


    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

