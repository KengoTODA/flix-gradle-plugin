plugins {
  `java-base`
  id("com.diffplug.spotless")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of("17")) } }

tasks.withType<JavaCompile> {
  options.release.set(8)
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    ktfmt()
    licenseHeader("/* (C) Kengo TODA \$YEAR */")
  }
  kotlinGradle { ktfmt() }
}
