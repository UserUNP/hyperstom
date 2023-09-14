plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // minestom
    implementation("dev.hollowcube:minestom-ce:e9d0098418")
    implementation("dev.hollowcube:polar:1.3.2")

    // logging
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    implementation("org.apache.commons:commons-compress:1.24.0")
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
