import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(platform("io.insert-koin:koin-bom:3.5.3"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.insert-koin:koin-core")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.insert-koin:koin-test")
    testImplementation("io.insert-koin:koin-test-junit4")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

mavenPublishing {
    coordinates("io.github.easynearby", "core", "0.0.2")

    pom {
        name.set("Nearby core library")
        description.set("Core library for EasyNearby")
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

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.20")
    }
}

tasks.withType<DokkaTask>().configureEach {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file(rootDir.resolve("assets/logo.png")))
        templatesDir = file(rootDir.resolve("dokka/templates"))
        customStyleSheets = listOf(file(rootDir.resolve("dokka/styles/logo-styles.css")))
    }
}