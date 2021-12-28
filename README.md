# Flix Gradle Plugin

An experimental Gradle plugin to build [Flix language](https://flix.dev/) projects.

[![.github/workflows/build.yml](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin+Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fjp%2Fskypencil%2Fflix%2Fflix-gradle-plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/jp.skypencil.flix)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e9d2cc3c9644462196d554e884ee4ce1)](https://www.codacy.com/gh/KengoTODA/flix-gradle-plugin/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=KengoTODA/flix-gradle-plugin&amp;utm_campaign=Badge_Grade)
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

## Getting Started
### Configure the plugin for existing Gradle projects

To integrate Flix into a Gradle project with other languages, apply the convention plugin `jp.skypencil.flix`:

```kotlin
plugins {
  `application`
  id("jp.skypencil.flix") version "1.0.0"
}
configure<JavaPluginExtension> {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
configure<FlixExtension> {
  compilerVersion.set("v0.25.0")
  sourceSets {
    main {
      setSrcDirs(["src/main/flix"])
    }
    test {
      setSrcDirs(["src/test/flix"])
    }
  }
}
```

Unlike common Flix projects, you need to locate files as follows:

* Source files should be stored under the `src/main/flix` directory.
* Test files should be stored under the `src/test/flix` directory.

### Configure the plugin for a pure Flix project

To build a Flix project created by `flix init` command, apply the base plugin `jp.skypencil.flix-base` and create necessary tasks individually:

```kotlin
import jp.skypencil.flix.FlixCompile
import jp.skypencil.flix.FlixTest
plugins {
    id("jp.skypencil.flix-base")
}

val compileFlix = tasks.register<FlixCompile>("compileFlix") {
    source = fileTree("src")
    destinationDirectory.set(file("build/classes/flix/main"))
}
val testFlix = tasks.register<FlixTest>("testFlix") {
    source = fileTree("test")
    destinationDirectory.set(file("build/classes/flix/test"))
    report.set(file("build/reports/flix/main.txt"))
}

tasks.named<Task>("assemble") { dependsOn(compileFlix) }
tasks.named<Task>("check") { dependsOn(testFlix) }
```

### Limitation

* No support for resource files such as `src/main/resources` and `src/test/resources`.
* Not tested with Flix v0.24.0 and older versions.
* Test reports generated at `build/reports/flix/main.txt` is quite simple.

## Developers' guideline
### TODO

* [x] integrate with `application` plugin
* [x] add `testFlix` task
* [x] add a task to make a `.fpkg` file
* [x] support the Gradle Java toolchain
* [x] use Gradle worker API to introduce the classloader level separation
* [ ] support dependency management (based on [an investigation](https://gist.github.com/KengoTODA/3598bcd784d2904948fc38e40fef637e))
* [ ] create a JUnit XML file based on test result

## Copyright

Copyright &copy; 2021 Kengo TODA
