plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    implementation("dev.hollowcube:minestom-ce:e9d0098418")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("hyperstom.infernity.dev.MainKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
