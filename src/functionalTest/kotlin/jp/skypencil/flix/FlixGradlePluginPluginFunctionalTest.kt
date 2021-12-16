/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

/** A simple functional test for the 'jp.skypencil.flix.greeting' plugin. */
class FlixGradlePluginPluginFunctionalTest {
  @get:Rule val tempFolder = TemporaryFolder()

  private fun getProjectDir() = tempFolder.root
  private fun getBuildFile() = getProjectDir().resolve("build.gradle")
  private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")

  @Test
  fun `can run task`() {
    // Setup the test build
    getSettingsFile().writeText("")
    getBuildFile().writeText("""
plugins {
    id('jp.skypencil.flix')
}
""")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":compileFlix")
    runner.withProjectDir(getProjectDir())
    val result = runner.build()

    // Verify the result
    assertEquals(TaskOutcome.NO_SOURCE, result.task(":compileFlix")?.outcome)
  }

  @Test
  fun `can compile files`() {
    // Setup the test build
    getSettingsFile().writeText("")
    getBuildFile().writeText("""
plugins {
    id('jp.skypencil.flix')
}
""")
    getProjectDir().resolve("src/main/flix").mkdirs()
    getProjectDir()
        .resolve("src/main/flix/Main.flix")
        .writeText(
            """
// The main entry point.
def main(_args: Array[String]): Int32 & Impure =
  Console.printLine("Hello World!");
  0 // exit code
""")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":compileFlix")
    runner.withProjectDir(getProjectDir())
    val result = runner.build()

    // Verify the result
    assertEquals(TaskOutcome.SUCCESS, result.task(":compileFlix")?.outcome)
    assertTrue(getProjectDir().resolve("build/classes/flix/main/Main.class").isFile)
  }

  @Test
  fun `can test files`() {
    // Setup the test build
    getSettingsFile().writeText("")
    getBuildFile().writeText("""
plugins {
    id('jp.skypencil.flix')
}
""")
    getProjectDir().resolve("src/main/flix").mkdirs()
    getProjectDir()
        .resolve("src/main/flix/Main.flix")
        .writeText(
            """
// The main entry point.
def main(_args: Array[String]): Int32 & Impure =
  Console.printLine("Hello World!");
  0 // exit code
""")
    getProjectDir().resolve("src/test/flix").mkdirs()
    getProjectDir()
        .resolve("src/test/flix/TestMain.flix")
        .writeText("""
@test
def test01(): Bool = 1 + 1 == 2
""")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":check")
    runner.withProjectDir(getProjectDir())
    val result = runner.build()

    // Verify the result
    assertEquals(TaskOutcome.SUCCESS, result.task(":testFlix")?.outcome)
    assertTrue(getProjectDir().resolve("build/reports/flix/main.txt").isFile)
  }

  @Test
  fun `can fail build in case of test failure`() {
    // Setup the test build
    getSettingsFile().writeText("")
    getBuildFile().writeText("""
plugins {
    id('jp.skypencil.flix')
}
""")
    getProjectDir().resolve("src/main/flix").mkdirs()
    getProjectDir()
        .resolve("src/main/flix/Main.flix")
        .writeText(
            """
// The main entry point.
def main(_args: Array[String]): Int32 & Impure =
  Console.printLine("Hello World!");
  0 // exit code
""")
    getProjectDir().resolve("src/test/flix").mkdirs()
    getProjectDir()
        .resolve("src/test/flix/TestMain.flix")
        .writeText("""
@test
def test01(): Bool = 1 + 1 == 0
""")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":check")
    runner.withProjectDir(getProjectDir())
    val result = runner.buildAndFail()

    // Verify the result
    assertEquals(TaskOutcome.FAILED, result.task(":testFlix")?.outcome)
    assertTrue(getProjectDir().resolve("build/reports/flix/main.txt").isFile)
  }

  @Test
  fun `can assemble fpkg file`() {
    // Setup the test build
    getSettingsFile().writeText("""
rootProject.name = "flix-project"
""")
    getBuildFile().writeText("""
plugins {
    id('jp.skypencil.flix')
}
""")
    getProjectDir().resolve("src/main/flix").mkdirs()
    getProjectDir()
        .resolve("src/main/flix/Main.flix")
        .writeText(
            """
// The main entry point.
def main(_args: Array[String]): Int32 & Impure =
  Console.printLine("Hello World!");
  0 // exit code
""")
    getProjectDir().resolve("README.md").writeText("# README")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":assemble")
    runner.withProjectDir(getProjectDir())
    val result = runner.build()

    // Verify the result
    assertEquals(TaskOutcome.SUCCESS, result.task(":fpkg")?.outcome)
    ZipFile(getProjectDir().resolve("build/fpkg/flix-project.fpkg")).use {
      assertNotNull(it.getEntry("Main.flix"))
      assertNotNull(it.getEntry("README.md"))
    }
  }

  @Test
  fun `can use toolchain`() {
    // Setup the test build
    getSettingsFile().writeText("")
    getBuildFile()
        .writeText(
            """
plugins {
    id('java') 
    id('jp.skypencil.flix')
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("17"))
    }
}
""")
    getProjectDir().resolve("src/main/flix").mkdirs()
    getProjectDir()
        .resolve("src/main/flix/Main.flix")
        .writeText(
            """
// The main entry point.
def main(_args: Array[String]): Int32 & Impure =
  Console.printLine("Hello World!");
  0 // exit code
""")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":compileFlix", "-S")
    runner.withProjectDir(getProjectDir())
    val result = runner.build()

    // Verify the result
    assertEquals(TaskOutcome.SUCCESS, result.task(":compileFlix")?.outcome)
    assertTrue(getProjectDir().resolve("build/classes/flix/main/Main.class").isFile)
  }
}
