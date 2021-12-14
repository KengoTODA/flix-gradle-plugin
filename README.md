# Flix Gradle Plugin

An experimental Gradle plugin to build [Flix language](https://flix.dev/) projects.

[![.github/workflows/build.yml](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/KengoTODA/flix-gradle-plugin/actions/workflows/build.yml)

## Project layout

Unlike the `flix` command, you need to locate files as follows:

* Source files should be stored under the `src/main/flix` directory.
* Test files should be stored under the `src/test/flix` directory.

## TODO

- [ ] integrate with `application` plugin
- [ ] add `testFlix` task
- [ ] support the Gradle Java toolchain
- [ ] support dependency management (based on [an investigation](https://gist.github.com/KengoTODA/3598bcd784d2904948fc38e40fef637e))
- [ ] create a JUnit XML file based on test result
- [ ] use Gradle worker API to introduce the classloader level separation

## Copyright

Copyright &copy; 2021 Kengo TODA
