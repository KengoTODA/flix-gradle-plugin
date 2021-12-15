# Flix Gradle Plugin

An experimental Gradle plugin to build [Flix language](https://flix.dev/) projects.

[![.github/workflows/build.yml](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml)

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

## Developers' guideline
### TODO

- [x] integrate with `application` plugin
- [ ] add `testFlix` task
- [x] add a task to make a `.fpkg` file
- [x] support the Gradle Java toolchain
- [ ] support dependency management (based on [an investigation](https://gist.github.com/KengoTODA/3598bcd784d2904948fc38e40fef637e))
- [ ] create a JUnit XML file based on test result
- [x] use Gradle worker API to introduce the classloader level separation

## Copyright

Copyright &copy; 2021 Kengo TODA
