
plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
}

val kotlin_version: String by project
val ktor_version: String by project
val logback_version: String by project
val jackson_version: String by project
val strikt_version: String by project
val detekt_version: String by project

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.strikt:strikt-core:$strikt_version")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detekt_version")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        dependsOn("cleanTest")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

ktor {
    fatJar {
        archiveFileName.set("developer-joyofenergy-kotlin-all.jar")
    }
}
