import de.undercouch.gradle.tasks.download.Download

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

val flixCompilerVersion = "v0.28.0"
tasks.register<Download>("downloadFlixCompiler") {
  src("https://github.com/flix/flix/releases/download/$flixCompilerVersion/flix.jar")
  dest("$buildDir/flix/flix-$flixCompilerVersion.jar")
  onlyIfModified(true)
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    ktfmt()
    licenseHeader("/* (C) Kengo TODA \$YEAR */")
  }
  kotlinGradle { ktfmt() }
  scala {
    scalafmt()
  }
}
