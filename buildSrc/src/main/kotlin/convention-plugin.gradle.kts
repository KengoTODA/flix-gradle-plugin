plugins {
  `java-base`
  id("com.diffplug.spotless")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("11")) } }

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    ktfmt()
    licenseHeader("/* (C) Kengo TODA \$YEAR */")
  }
  kotlinGradle { ktfmt() }
}
