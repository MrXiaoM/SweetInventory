rootProject.name = "SweetInventory"
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("top.mrxiaom:shadow:7.1.3")
    }
}
include(":paper")
