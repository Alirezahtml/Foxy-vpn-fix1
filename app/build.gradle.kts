import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val debugKeystoreFile = rootProject.file("debug.keystore")
val debugKeystoreBase64File = rootProject.file("debug.keystore.base64")
if (!debugKeystoreFile.exists() && debugKeystoreBase64File.exists()) {
    runCatching {
        val bytes = Base64.getDecoder().decode(debugKeystoreBase64File.readText().trim())
        debugKeystoreFile.writeBytes(bytes)
    }
}

val explicitReleaseKeystore = rootProject.file("release-keystore.jks")
val hasExplicitReleaseEnv = explicitReleaseKeystore.exists() &&
    !System.getenv("SIGNING_KEY_ALIAS").isNullOrBlank() &&
    !System.getenv("SIGNING_KEY_PASSWORD").isNullOrBlank() &&
    !System.getenv("SIGNING_STORE_PASSWORD").isNullOrBlank()

val releaseKeystoreFile = explicitReleaseKeystore
val releaseKeyAlias: String? = if (hasExplicitReleaseEnv) System.getenv("SIGNING_KEY_ALIAS") else null
val releaseKeyPassword: String? = if (hasExplicitReleaseEnv) System.getenv("SIGNING_KEY_PASSWORD") else null
val releaseStorePassword: String? = if (hasExplicitReleaseEnv) System.getenv("SIGNING_STORE_PASSWORD") else null
val hasReleaseSigning = releaseKeystoreFile.exists() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank()

android {
    namespace = "com.vauth.foxyvpn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vauth.foxyvpn.neu"
        minSdk = 26
        targetSdk = 35
        versionCode = 76
        versionName = "2.0.0"
    }

    signingConfigs {
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debugConfig")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    val enableAbiSplits = project.findProperty("enableAbiSplits")?.toString()?.toBoolean() == true
    splits {
        abi {
            isEnable = enableAbiSplits
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/*.SF")
        resources.excludes.add("META-INF/*.DSA")
        resources.excludes.add("META-INF/*.RSA")
    }
}

val libboxVersion = "v1.15.0-foxy-masque.2"
val libboxExpectedSha256 = "f1b59f4fab207b4c19f8ea3d145bc4dd0e834a530081d0dc045f4b795d6eac0b"
val libboxDownloadUrl = "https://github.com/Vauth/sing-box/releases/download/$libboxVersion/libbox.aar"
val libboxFile = file("libs/libbox.aar")

fun computeFileSha256(target: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    target.inputStream().buffered().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var read = input.read(buffer)
        while (read >= 0) {
            if (read > 0) digest.update(buffer, 0, read)
            read = input.read(buffer)
        }
    }
    return digest.digest().joinToString("") { b: Byte -> "%02x".format(b) }
}

val ensureLibboxAar by tasks.registering {
    outputs.file(libboxFile)
    doLast {
        if (libboxFile.exists() && computeFileSha256(libboxFile).equals(libboxExpectedSha256, ignoreCase = true)) {
            return@doLast
        }
        libboxFile.parentFile.mkdirs()
        logger.lifecycle("Downloading sing-box core ($libboxVersion) from $libboxDownloadUrl ...")
        val tempFile = file("libs/libbox.aar.tmp")
        URI(libboxDownloadUrl).toURL().openStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val actualSha = computeFileSha256(tempFile)
        if (!actualSha.equals(libboxExpectedSha256, ignoreCase = true)) {
            tempFile.delete()
            throw GradleException("libbox.aar SHA-256 mismatch! Expected $libboxExpectedSha256 but got $actualSha")
        }
        if (libboxFile.exists()) libboxFile.delete()
        tempFile.renameTo(libboxFile)
    }
}

tasks.named("preBuild") {
    dependsOn(ensureLibboxAar)
}

tasks.configureEach {
    if (name.startsWith("package") && name.endsWith("Release")) {
        doFirst {
            if (!hasReleaseSigning) {
                logger.lifecycle("Using fallback debugConfig signing for Release APK.")
            }
        }
    }
}

dependencies {
    implementation(files("libs/libbox.aar"))

    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.4.0-alpha10")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.core:core-ktx:1.15.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
