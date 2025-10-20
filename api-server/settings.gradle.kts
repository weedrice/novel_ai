rootProject.name = "api-server"

// Gradle Toolchain Resolver Plugin을 사용하여 JDK 자동 다운로드
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
