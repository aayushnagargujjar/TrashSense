plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.trashsense"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }
    defaultConfig {
        applicationId = "com.example.trashsense"
        minSdk = 24
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
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.cast.tv)
    implementation(libs.androidx.runner)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.recyclerview)
    //tf lite
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


   //Material Components dependency
    implementation (libs.material.v1110)
    //cloudinary
    implementation (libs.cloudinary.android)
    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    //Glide work seamlessly with cloudinary
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.16.0")
      //chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // For OkHttp (network requests)
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    //retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")


    /* //Bottom nav bar
     implementation (libs.bubblenavigation)
     implementation(libs.meow.bottom.navigation)
     //for getting git project
     implementation(libs.meowbottomnavigation)*/

}
