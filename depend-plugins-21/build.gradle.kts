plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.nexomc.com/releases")
}

val targetJavaVersion = 21

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("top.mrxiaom.pluginbase:library:1.7.6")
    // CraftEngine
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")
    // Nexo
    compileOnly("com.nexomc:nexo:1.19.1")
    compileOnly(rootProject)
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}
