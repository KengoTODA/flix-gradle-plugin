plugins {
  `kotlin-dsl`
  id("com.diffplug.spotless") version "6.7.2"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("17")) } }

dependencies {
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.8.0")
  implementation("de.undercouch:gradle-download-task:5.1.1")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> { kotlinGradle { ktfmt() } }
