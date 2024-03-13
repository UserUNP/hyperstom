import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.bedcrab.hyperstom"
version = "1.0.0-alpha"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(libs.logging)
    implementation(libs.logging.impl)
    implementation(libs.logging.core)
    implementation(libs.minestom)
    implementation(libs.minestom.polar)
    implementation(libs.compression)
    implementation(libs.serialization.core)
    implementation(libs.serialization.cbor)
    implementation(libs.coroutines)
}

kotlin {
    jvmToolchain(21)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("hyperstom")
    manifest {
        attributes("Main-Class" to "dev.bedcrab.hyperstom.MainKt")
    }
}

application {
    mainClass.set("dev.bedcrab.hyperstom.MainKt")
    tasks.run.get().workingDir = File(rootProject.projectDir, ".run")
}
