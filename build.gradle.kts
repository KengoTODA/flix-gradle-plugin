plugins {
  `java-gradle-plugin`
  `convention-plugin`
  `maven-publish`
  id("com.gradle.plugin-publish") version "0.21.0"
  id("org.jetbrains.dokka") version "1.6.21"
  id("org.jetbrains.kotlin.jvm") version "1.6.21"
}

group = "jp.skypencil.flix"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

val flixCompiler = tasks.downloadFlixCompiler.map { it.outputs.files }

dependencies {
  implementation("de.undercouch:gradle-download-task:5.0.5")
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation(project("modules:packager-shell"))

  compileOnly(flixCompiler)

  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation(flixCompiler)
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

tasks.dokkaHtml.configure { outputDirectory.set(buildDir.resolve("reports").resolve("dokka")) }

tasks.named<Task>("check") { dependsOn(functionalTest) }

tasks.named<Task>("build") { dependsOn(tasks.dokkaHtml) }

pluginBundle {
  website = "https://github.com/KengoTODA/flix-gradle-plugin"
  vcsUrl = "https://github.com/KengoTODA/flix-gradle-plugin"
  tags = listOf("flix")
}

publishing {
  publications.withType<MavenPublication> {
    pom {
      scm {
        connection.set("git@github.com:KengoTODA/flix-gradle-plugin.git")
        developerConnection.set("git@github.com:KengoTODA/flix-gradle-plugin.git")
        url.set("https://github.com/KengoTODA/flix-gradle-plugin")
      }
      licenses {
        license {
          name.set("GNU Affero General Public License v3.0")
          url.set("https://www.gnu.org/licenses/agpl-3.0.en.html")
        }
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions { jvmTarget = "1.8" }
}

defaultTasks("spotlessApply", "build")
