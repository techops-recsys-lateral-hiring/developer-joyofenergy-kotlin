pluginManagement {
    val kotlin_version: String by settings
    val ktor_version: String by settings
    val detekt_version: String by settings
    plugins {
        kotlin("jvm") version kotlin_version
        id("io.ktor.plugin") version ktor_version
        id("io.gitlab.arturbosch.detekt") version detekt_version
    }
}

rootProject.name = "developer-joyofenergy-kotlin"
