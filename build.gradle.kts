import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "tk.roman.salariumautologin"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
compose.desktop {
    application {
        nativeDistributions {
            packageName = "Salarium Scheduler V2"
            version = "v0.7"
            description = "Application to automatically log into salarium"
            copyright = "© 2020 Roman Augusto. All rights reserved."
            macOS {
                packageName = "Salarium Scheduler V2"
                version = "v0.7"
                description = "Application to automatically log into salarium"
                copyright = "© 2022 Roman Augusto. All rights reserved."
                iconFile.set(project.file("src/jvmMain/resources/drawable/icon.icns"))
                jvmArgs("-Dapple.awt.application.appearance=system")

            }
        }
    }
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }

    }

    kotlin.sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.exposed:exposed-jodatime:0.39.2")
                implementation("org.seleniumhq.selenium:selenium-java:4.4.0")
                implementation("org.json:json:20220320")

            }

            resources.srcDirs("/src/jvmMain/resources/driver/chromedriver")
            resources.filter {
                it.exists()
            }
        }
        val jvmTest by getting
    }
}



compose.desktop {
    application {
        mainClass = "Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Salarium Scheduler V2"
            packageVersion = "1.0.0"
        }
    }
}
