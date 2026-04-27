plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "io.github.tessG"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()

    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
    }
}

tasks.test {
    useJUnitPlatform()
}

intellijPlatform {
    pluginConfiguration {
        name = "Processing Project Generator"
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "251.*"
        }
    }
}
tasks {
    buildSearchableOptions {
        enabled = false
    }
}