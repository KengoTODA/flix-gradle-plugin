import de.undercouch.gradle.tasks.download.Download

plugins {
  `java-gradle-plugin`
  id("org.jetbrains.kotlin.jvm") version "1.6.0"
  id("com.diffplug.spotless") version "6.0.4"
  id("de.undercouch.download") version "4.1.2"
}

val flixCompilerVersion = "v0.25.0"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("11")) } }

val downloadFlixCompiler =
    tasks.register<Download>("downloadFlixCompiler") {
      src("https://github.com/flix/flix/releases/download/$flixCompilerVersion/flix.jar")
      dest("$buildDir/flix/flix-$flixCompilerVersion.jar")
      onlyIfModified(true)
    }

val processVersionFile =
    tasks.register<WriteProperties>("processVersionFile") {
      outputFile = file("$buildDir/resources/main/flix-gradle-plugin.properties")
      property("compiler-version", flixCompilerVersion)
    }

tasks.processResources { dependsOn(processVersionFile) }

dependencies {
  implementation(downloadFlixCompiler.map { it.outputs.files })
  implementation("de.undercouch:gradle-download-task:4.1.2")

  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Use the Kotlin JDK 8 standard library.
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
  val flixPlugin by
      plugins.creating {
        id = "jp.skypencil.flix"
        implementationClass = "jp.skypencil.flix.FlixPlugin"
      }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val functionalTest by
    tasks.registering(Test::class) {
      testClassesDirs = functionalTestSourceSet.output.classesDirs
      classpath = functionalTestSourceSet.runtimeClasspath
    }

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") { dependsOn(functionalTest) }

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    ktfmt()
    licenseHeader("/* (C) Kengo TODA \$YEAR */")
  }
  kotlinGradle { ktfmt() }
}

defaultTasks("spotlessApply", "build")
