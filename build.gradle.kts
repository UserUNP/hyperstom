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
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    implementation("net.minestom:minestom-snapshots:7e59603d5f")
    implementation("net.kyori:adventure-text-minimessage:4.16.0")
    implementation("dev.hollowcube:polar:1.7.2")
    
    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.6.3")
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
