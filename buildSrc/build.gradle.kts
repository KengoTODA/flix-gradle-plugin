plugins {
  `kotlin-dsl`
  id("com.diffplug.spotless") version "6.5.2"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("17")) } }

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.5.2")
  implementation("de.undercouch:gradle-download-task:5.1.0")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> { kotlinGradle { ktfmt() } }
