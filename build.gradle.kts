import de.undercouch.gradle.tasks.download.Download

plugins {
  `java-gradle-plugin`
  `convention-plugin`
  id("com.gradle.plugin-publish") version "0.18.0"
  id("de.undercouch.download") version "4.1.2"
  id("org.jetbrains.dokka") version "1.6.0"
  id("org.jetbrains.kotlin.jvm") version "1.6.10"
}

val flixCompilerVersion = "v0.25.0"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

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

tasks.named<Task>("processResources") { dependsOn(processVersionFile) }

dependencies {
  implementation("de.undercouch:gradle-download-task:4.1.2")
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  compileOnly(downloadFlixCompiler.map { it.outputs.files })

  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation(downloadFlixCompiler.map { it.outputs.files })
}

gradlePlugin {
  val flixPlugin by
      plugins.creating {
        id = "jp.skypencil.flix"
        displayName = "Flix plugin"
        description = "A Gradle plugin which provides conventions for Flix projects"
        implementationClass = "jp.skypencil.flix.FlixPlugin"
      }
  val flixBasePlugin by
      plugins.creating {
        id = "jp.skypencil.flix-base"
        displayName = "Flix base plugin"
        description = "A Gradle plugin which provides tasks and an extension for Flix projects"
        implementationClass = "jp.skypencil.flix.FlixBasePlugin"
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

val dokkaHtml =
    tasks.dokkaHtml.configure { outputDirectory.set(buildDir.resolve("reports").resolve("dokka")) }

tasks.named<Task>("check") { dependsOn(functionalTest) }

tasks.named<Task>("build") { dependsOn(dokkaHtml) }

pluginBundle {
  website = "https://github.com/KengoTODA/flix-gradle-plugin"
  vcsUrl = "git@github.com:KengoTODA/flix-gradle-plugin.git"
  tags = listOf("flix")
}

defaultTasks("spotlessApply", "build")
