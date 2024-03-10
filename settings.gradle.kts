plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "hyperstom"
include("code", "server")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("minestom", "net.minestom:minestom-snapshots:7aaa85cd47")
            library("minestom-polar", "dev.hollowcube:polar:1.7.2")
            library("plainSerializer", "net.kyori:adventure-text-serializer-plain:4.16.0")

            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

            library("cbor", "org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.6.3")
            library("compression", "org.apache.commons:commons-compress:1.24.0")

            library("logging", "io.github.oshai:kotlin-logging-jvm:5.1.0")
            library("logging-impl", "org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
            library("logging-core", "org.apache.logging.log4j:log4j-core:2.20.0")
        }
    }
}
