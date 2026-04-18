import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.19")
}
val base = LibraryHelper(project)

group = "top.mrxiaom.sweet.inventory"
version = "1.0.3"

val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, paper, gui, actions, l10n, misc) }
val shadowGroup = "top.mrxiaom.sweet.inventory.libs"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvn.lumine.io/repository/maven/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://r.irepo.space/maven/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.nexomc.com/releases")
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.20") // NMS

    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    // MythicMobs
    compileOnly("io.lumine:Mythic-Dist:4.13.0")
    compileOnly("io.lumine:Mythic:5.6.2")
    compileOnly("io.lumine:LumineUtils:1.20-SNAPSHOT")
    // MythicLib, MMOItems
    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")
    // NeigeItems
    compileOnly("pers.neige.neigeitems:NeigeItems:1.21.128")
    // CraftEngine
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")
    // Nexo
    compileOnly("com.nexomc:nexo:1.19.1")
    // SX-Item
    compileOnly("com.github.Saukiya:SX-Item:4.4.9")

    compileOnly("org.black_ixx:playerpoints:3.2.7")
    compileOnly("com.github.dmulloy2:ProtocolLib:5.3.0")
    compileOnly(base.depend.annotations)

    base.library(LibraryHelper.adventure("4.22.0"))

    implementation(base.depend.nbtapi)
    implementation(base.depend.EvalEx)
    implementation("commons-io:commons-io:2.21.0")
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)

    testImplementation("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
}
buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.inventory")

    base.doResolveLibraries()

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

LibraryHelper.initJava(project, base, targetJavaVersion, true)
LibraryHelper.initPublishing(project)

tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "de.tr7zw.changeme.nbtapi" to "nbtapi",
            "com.ezylang.evalex" to "evalex",
            "com.tcoded.folialib" to "folialib",
            "org.apache.commons.io" to "commons-io"
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
