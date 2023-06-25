import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm")
}

val kotlin_version: String by project
val ktor_version: String by project
val logback_version: String by project
val jackson_version: String by project
val strikt_version: String by project


val appMainClass = "io.ktor.server.netty.EngineMain"

application {
    mainClass.set(appMainClass)
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
    implementation("io.ktor:ktor-jackson:$ktor_version")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.strikt:strikt-core:$strikt_version")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        //dependsOn("cleanTest")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.jar {
    manifest.attributes["Main-Class"] = appMainClass
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
