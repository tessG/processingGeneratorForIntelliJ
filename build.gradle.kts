plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "io.github.tessG"
version = "1.0.1"

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
        intellijIdeaCommunity("2024.3")
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
            untilBuild = provider { null }
        }
    }
}
tasks {
    buildSearchableOptions {
        enabled = false
    }
}