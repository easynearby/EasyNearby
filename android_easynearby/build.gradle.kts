import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
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

    dependencies {
        implementation("androidx.startup:startup-runtime:1.1.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("com.google.android.gms:play-services-nearby:18.5.0")
        api(project(":core"))
        implementation("com.jakewharton.timber:timber:5.0.1")

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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

mavenPublishing {
    coordinates("io.github.easynearby", "android", "0.0.2")

    pom {
        name.set("Android Nearby library")
        description.set("Android implementation of Nearby core library")
        inceptionYear.set("2024")
        url.set("https://github.com/easynearby/EasyNearby")
        licenses {
            license {
                name.set("GNU GENERAL PUBLIC LICENSE VERSION 3.0")
                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                distribution.set("https://www.gnu.org/licenses/gpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("gusakovgiorgi")
                name.set("Giorgi Gusakov")
                url.set("https://github.com/gusakovgiorgi")
            }
        }
        scm {
            url.set("https://github.com/easynearby/EasyNearby")
            connection.set("scm:git:git://github.com/easynearby/EasyNearby.git")
            developerConnection.set("scm:git:ssh://git@github.com:easynearby/EasyNearby.git")
        }
    }
}