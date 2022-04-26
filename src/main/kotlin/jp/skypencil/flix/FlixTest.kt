/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.language.ast.Symbol
import ca.uwaterloo.flix.tools.`Tester$`
import javax.inject.Inject
import jp.skypencil.flix.internal.`PackagerShell$`
import jp.skypencil.flix.internal.WorkQueueFactory
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

/**
 * Task to run Flix test cases.
 *
 * Here is an example to register a task to run Flix test cases located in the `test` directory:
 *
 * ```kotlin
 * tasks.register<FlixCompile>("testFlix") {
 *   source = fileTree("test")
 *   destinationDirectory.set(file("build/classes/flix/test"))
 *   report.set(file("build/reports/flix/main.txt"))
 * }
 * ```
 *
 * @since 1.0
 */
@CacheableTask
abstract class FlixTest : AbstractCompile() {
  @get:Nested
  @get:Optional
  val launcher: Property<JavaLauncher> = project.objects.property(JavaLauncher::class.java)

  @Inject abstract fun getWorkerExecutor(): WorkerExecutor

  private val workQueueFactory: WorkQueueFactory = WorkQueueFactory(logger, getWorkerExecutor())

  @get:OutputFile val report: RegularFileProperty = project.objects.fileProperty()

  @TaskAction
  fun run() {
    val workQueue = workQueueFactory.createWorkQueue(launcher, classpath)
    workQueue.submit(TestAction::class.java) {
      it.getDestinationDirectory().set(destinationDirectory)
      it.getSource().from(source)
      it.getClasspath().from(classpath)
      it.getTextReport().set(report)
    }
  }
}

private data class TestResult(val success: Boolean, val sym: Symbol.DefnSym, val message: String?)

abstract class TestAction : WorkAction<TestParameter> {
  override fun execute() {
    val options =
        `PackagerShell$`.`MODULE$`.createOptions(
            parameters.getDestinationDirectory().get().asFile.toPath())

    val flix = Flix()
    parameters.getSource().asFileTree.matching { it.include("*.flix") }.forEach {
      flix.addSourcePath(it.toPath())
    }
    parameters.getClasspath().forEach {
      when {
        it.name.endsWith(".fpkg") -> flix.addSourcePath(it.toPath())
        it.name.endsWith(".jar") -> flix.addJar(it.toPath())
        else -> {
          // logger.debug("{} found in the compile classpath but ignored", it.toPath())
        }
      }
    }

    flix.setOptions(options)
    val compileResult = flix.compile()
    val results = `Tester$`.`MODULE$`.test(compileResult)
    if (parameters.getTextReport().isPresent) {
      parameters.getTextReport().get().asFile.bufferedWriter().use { writer ->
        results.foreach {
          writer.write(it.toString())
          writer.newLine()
        }
      }
    }
    if (results.exists { !it.success }) {
      if (parameters.getTextReport().isPresent) {
        throw GradleException(
            "Flix test failed, see the report generated at ${ parameters.getTextReport().get().asFile.absolutePath }")
      } else {
        throw GradleException("Flix test failed")
      }
    }
  }
}

interface TestParameter : WorkParameters {
  // TODO Cannot pass ConfigurableFileTree, so using FileCollection as workaround
  // https://github.com/gradle/gradle/issues/18770
  // https://github.com/gradle/gradle/issues/19174
  fun getSource(): ConfigurableFileCollection
  fun getClasspath(): ConfigurableFileCollection
  fun getDestinationDirectory(): DirectoryProperty
  fun getTextReport(): RegularFileProperty
}
