pluginManagement {
    val kotlin_version: String by settings
    plugins {
        kotlin("jvm") version kotlin_version
    }
}

rootProject.name = "developer-joyofenergy-kotlin"
