plugins {
  `kotlin-dsl`
  id("com.diffplug.spotless") version "6.2.1"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("17")) } }

dependencies { implementation("com.diffplug.spotless:spotless-plugin-gradle:6.2.0") }

configure<com.diffplug.gradle.spotless.SpotlessExtension> { kotlinGradle { ktfmt() } }
