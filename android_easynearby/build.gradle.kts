plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}

android {
    namespace = "io.github.easynearby.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    publishing {
        singleVariant("release"){
            withSourcesJar()
            withJavadocJar()
        }
    }

    dependencies {
        implementation("androidx.startup:startup-runtime:1.1.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("com.google.android.gms:play-services-nearby:18.5.0")
        api(project(":core"))
        implementation("androidx.core:core-ktx:1.9.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")

        // Core library
        testImplementation("androidx.test:core:1.5.0")

        // AndroidJUnitRunner and JUnit Rules
        testImplementation("androidx.test:runner:1.5.2")
        testImplementation("androidx.test:rules:1.5.0")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

        // Assertions
        testImplementation("androidx.test.ext:junit:1.1.5")

        testImplementation("org.robolectric:robolectric:4.10.3")

        testImplementation("io.mockk:mockk:1.13.10")

        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    }
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "io.github.easynearby"
                artifactId = "android"
                version = "0.0.1"
                from(components["release"])
            }
        }
        repositories {
            maven {
                name = "myrepo"
                url = uri("${project.buildDir}/repo")
            }
        }
    }
}
