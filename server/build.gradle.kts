plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "dev.bedcrab.hypersquare"
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
    implementation(libs.plainSerializer)

    implementation(libs.coroutines)
    implementation(libs.cbor)
    implementation(libs.compression)

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.bedcrab.hyperstom.MainKt")
    tasks.run.get().workingDir = File("../.run")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
