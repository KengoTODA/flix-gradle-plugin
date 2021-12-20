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
  private fun getBuildFile() = getProjectDir().resolve("build.gradle.kts")
  private fun getSettingsFile() = getProjectDir().resolve("settings.gradle.kts")

  @Test
  fun `can set FlixCompile task`() {
    getSettingsFile().writeText("")
    getBuildFile()
        .writeText(
            """
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
        .resolve("test/TestMain.flix")
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
