/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
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
        .resolve("src/main/flix/main.flix")
        .writeText(
            """
enum Shape {
    case Circle(Int32),
    case Square(Int32),
    case Rectangle(Int32, Int32)
}

pub def area(s: Shape): Int32 = match s {
    case Circle(r)       => 3 * (r * r)
    case Square(w)       => w * w
    case Rectangle(h, w) => h * w
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
    assertEquals(TaskOutcome.SUCCESS, result.task(":compileFlix")?.outcome)
    System.err.println(
        Arrays.toString(getProjectDir().resolve("build/classes/flix/main").listFiles()))
    // TODO why no Shape.class file?
    assertTrue(getProjectDir().resolve("build/classes/flix/main/RecordEmpty.class").isFile)
  }
}
