import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")

}

group = "tk.roman.salariumautologin"
version = "1.3.0"


repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}



compose.desktop {
    application {
        nativeDistributions {
            packageName = "Salarium Scheduler V2"
            version = "1.3.0"
            description = "Application to automatically log into salarium"
            copyright = "© 2022 Roman Augusto. All rights reserved."
            macOS {
                packageName = "Salarium Scheduler V2"
                version = "1.3.0"
                description = "Application to automatically log into salarium"
                copyright = "© 2022 Roman Augusto. All rights reserved."
                iconFile.set(project.file("src/jvmMain/resources/drawable/icon.icns"))
                jvmArgs("-Dapple.awt.application.appearance=system")

            }
            windows {
                packageName = "Salarium Scheduler v2"
                iconFile.set(project.file("src/jvmMain/resources/drawable/icon.ico"))
                shortcut = true
                menuGroup = "Salarium Scheduler"
                menu = true
            }
        }
    }
}
kotlin {
    jvm {

        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }

    }

    kotlin.sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.exposed:exposed-jodatime:0.39.2")
                implementation("org.seleniumhq.selenium:selenium-java:4.4.0")
                implementation("org.json:json:20220320")
                implementation("net.axay:simplekotlinmail-core:1.4.0")
                implementation("net.axay:simplekotlinmail-client:1.4.0")
                implementation("org.slf4j:slf4j-api:1.7.5")
                implementation("org.slf4j:slf4j-simple:1.6.4")
            }
        }
        val jvmTest by getting

    }
}




compose.desktop {
    application {
        mainClass = "tk.romanaugusto.Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Salarium Scheduler V2"
            packageVersion = "1.3.0"
        }
    }
}
