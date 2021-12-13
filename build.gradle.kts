plugins {
  `java-gradle-plugin`
  id("org.jetbrains.kotlin.jvm") version "1.6.0"
  id("com.diffplug.spotless") version "6.0.4"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of("11")) } }

dependencies {
  implementation(files("lib/flix.jar"))
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
