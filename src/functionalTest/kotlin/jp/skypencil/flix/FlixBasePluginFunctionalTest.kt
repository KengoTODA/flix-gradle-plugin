/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import kotlin.test.Test
import kotlin.test.assertEquals
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class FlixBasePluginFunctionalTest {
  @get:Rule val tempFolder = TemporaryFolder()

  private fun getProjectDir() = tempFolder.root
  private fun getBuildFile() = getProjectDir().resolve("build.gradle")
  private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")

  @Test
  fun `can set FlixCompile task`() {
    getSettingsFile().writeText("")
    getBuildFile()
        .writeText(
            """
plugins {
    id('jp.skypencil.flix-base')
}
val flixCompile = tasks.register<FlixCompile>("flixCompile") {
    source = fileTree("src")
    destinationDirectory.set(file("build/classes/flix/main")
}
val flixTest = tasks.register<FlixTest>("flixTest") {
    source = fileTree("test")
    destinationDirectory.set(file("build/classes/flix/test")
}
tasks.named<Task>("classes") { dependsOn(flixCompile) }
tasks.named<Task>("check") { dependsOn(flixTest) }
""")

    getProjectDir().resolve("src").mkdirs()
    getProjectDir()
        .resolve("src/Main.flix")
        .writeText(
            """
// The main entry point.
def main(_args: Array[String]): Int32 & Impure =
  Console.printLine("Hello World!");
  0 // exit code
""")
    getProjectDir().resolve("test").mkdirs()
    getProjectDir()
        .resolve("src/TestMain.flix")
        .writeText("""
@test
def test01(): Bool = 1 + 1 == 2
""")

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(":build")
    runner.withProjectDir(getProjectDir())
    val result = runner.build()

    // Verify the result
    assertEquals(TaskOutcome.SUCCESS, result.task(":compileFlix")?.outcome)
    assertEquals(TaskOutcome.SUCCESS, result.task(":testFlix")?.outcome)
  }
}
