plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = properties["pluginGroup"]!!
version = properties["pluginVersion"]!!

repositories {
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/") //paper
    maven(url = "https://repo.dmulloy2.net/nexus/repository/public/") //protocollib
    maven(url = "https://jitpack.io/") //tap, psychic
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8")) //kotlin
    compileOnly("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT") //paper
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0") //protocollib
    compileOnly("com.github.noonmaru:tap:2.3.3") //tap

    implementation("com.github.noonmaru:kommand:0.1.9")
    implementation("it.unimi.dsi:fastutil:8.3.1")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
    shadowJar {
        archiveClassifier.set("dist")
    }
    create<Copy>("distJar") {
        from(shadowJar)
        into("W:\\Servers\\2020-05-22 043000-paper-300\\plugins")
    }
}

if (!hasProperty("debug")) {
    tasks {
        shadowJar {
            relocate("it.unimi.dsi", "com.github.noonmaru.farm.internal.it.unimi.dsi")
        }
    }
}
