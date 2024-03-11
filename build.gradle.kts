import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    group = "dev.bedcrab.hyperstom"
    version = "1.0.0-alpha"

    repositories {
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }

    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    implementation(project(":mc-server"))
    implementation(project(":world"))

    implementation(libs.logging)
    implementation(libs.logging.impl)
    implementation(libs.logging.core)
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
