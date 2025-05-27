plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.jacoco)
}

android {
    namespace = "com.sample.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sample.android"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
        compose = true
        viewBinding = true
        dataBinding = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.extensions.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

private val modules = listOf("app", "network", "data")
tasks.register("jacocoTestReport") {
    group = "verification"
    description = "Runs all unit tests to generate .exec files"

    dependsOn(modules.map { ":$it:testDebugUnitTest" })
    finalizedBy("jacocoTestReportMerged")
}

tasks.register<JacocoReport>("jacocoTestReportMerged") {
    val execFiles = modules.map {
        file("$rootDir/$it/build/jacoco/testDebugUnitTest.exec")
    }

    val classDirs = modules.map {
        fileTree("$rootDir/$it/build/tmp/kotlin-classes/debug") {
            exclude(
                "**/R.class", "**/R$*.class",
                "**/BuildConfig.*", "**/Manifest*.*",
                "**/*Test*.*", "**/Hilt_*.class"
            )
        }
    }

    val srcDirs = modules.flatMap {
        listOf(
            file("$rootDir/$it/src/main/java"),
            file("$rootDir/$it/src/main/kotlin")
        )
    }

    executionData.setFrom(files(execFiles))
    classDirectories.setFrom(files(classDirs))
    sourceDirectories.setFrom(files(srcDirs))

    reports {
        html.required.set(true)
        xml.required.set(true)
        html.outputLocation.set(file("$buildDir/reports/jacoco/html"))
    }
}

jacoco {
    toolVersion = "0.8.10"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.glide)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.testng)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(project(":data"))
    implementation(project(":network"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.okhttp3.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(project(":data"))
    testImplementation(project(":network"))
}