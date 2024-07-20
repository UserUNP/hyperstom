import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "userunp.hyperstom"
version = "1.0.0-alpha"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("net.minestom:minestom-snapshots:1f34e60ea6")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("dev.hollowcube:polar:1.11.0")

    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation(kotlin("reflect"))
}

kotlin {
    jvmToolchain(21)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("hyperstom")
    manifest {
        attributes("Main-Class" to "ma.userunp.hyperstom.MainKt")
    }
}

application {
    mainClass.set("ma.userunp.hyperstom.MainKt")
    tasks.run.get().workingDir = File(rootProject.projectDir, ".run")
}
