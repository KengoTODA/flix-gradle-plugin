# Flix Gradle Plugin

An experimental Gradle plugin to build [Flix language](https://flix.dev/) projects.

[![.github/workflows/build.yml](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml)
<!-- TODO add a Gradle plugins portal badge -->
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

## Getting Started
### Project layout

Unlike the `flix` command, you need to locate files as follows:

* Source files should be stored under the `src/main/flix` directory.
* Test files should be stored under the `src/test/flix` directory.

### Plugin Configuration

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

### Limitation

* No support for resource files such as `src/main/resources` and `src/test/resources`
* Not tested with v0.24.0 or older
* Test reports generated at `build/reports/flix/main.txt` is quite simple

## Developers' guideline
### TODO

- [x] integrate with `application` plugin
- [x] add `testFlix` task
- [x] add a task to make a `.fpkg` file
- [x] support the Gradle Java toolchain
- [x] use Gradle worker API to introduce the classloader level separation
- [ ] support dependency management (based on [an investigation](https://gist.github.com/KengoTODA/3598bcd784d2904948fc38e40fef637e))
- [ ] create a JUnit XML file based on test result

## Copyright

Copyright &copy; 2021 Kengo TODA
