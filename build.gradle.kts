import de.undercouch.gradle.tasks.download.Download

plugins {
  `java-gradle-plugin`
  `convention-plugin`
  id("org.jetbrains.kotlin.jvm") version "1.6.10"
  id("de.undercouch.download") version "4.1.2"
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

defaultTasks("spotlessApply", "build")
