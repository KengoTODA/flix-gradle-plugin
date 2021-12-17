plugins {
  `kotlin-dsl`
  id("com.diffplug.spotless") version "6.0.5"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("11")) } }

dependencies { implementation("com.diffplug.spotless:spotless-plugin-gradle:6.0.5") }

configure<com.diffplug.gradle.spotless.SpotlessExtension> { kotlinGradle { ktfmt() } }
