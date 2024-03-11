plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "hyperstom"
include("commons", "code", "mc-server")
include("world")
