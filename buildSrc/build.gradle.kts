plugins {
  `kotlin-dsl`
  id("com.diffplug.spotless") version "6.3.0"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("17")) } }

dependencies { implementation("com.diffplug.spotless:spotless-plugin-gradle:6.4.1") }

configure<com.diffplug.gradle.spotless.SpotlessExtension> { kotlinGradle { ktfmt() } }
