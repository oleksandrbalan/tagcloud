import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.cocoapods)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.library)
    id("convention.jvm.toolchain")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    androidTarget()

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Demo Compose Multiplatform module"
        homepage = "---"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosdemo/Podfile")
        framework {
            baseName = "demo"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":tagcloud"))
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }

        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
            languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
        }
    }
}

android {
    namespace = "eu.wewox.tagcloud.demo"

    compileSdk = libs.versions.sdk.compile.get().toInt()

    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
}
