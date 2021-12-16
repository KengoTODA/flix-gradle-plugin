/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.language.CompilationMessage
import ca.uwaterloo.flix.language.ast.Symbol
import ca.uwaterloo.flix.util.Options
import ca.uwaterloo.flix.util.vt.TerminalContext
import javax.inject.Inject
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import scala.Function0
import scala.Tuple2

@CacheableTask
abstract class FlixTest : AbstractCompile() {
  @get:Nested
  @get:Optional
  val launcher: Property<JavaLauncher> = project.objects.property(JavaLauncher::class.java)

  @Inject abstract fun getWorkerExecutor(): WorkerExecutor

  @TaskAction
  fun run() {
    val workQueue =
        if (launcher.isPresent) {
          getWorkerExecutor().processIsolation { workerSpec ->
            workerSpec.classpath.from(classpath)
            workerSpec.forkOptions.setExecutable(launcher.get().executablePath)
          }
        } else {
          getWorkerExecutor().classLoaderIsolation { workerSpec ->
            workerSpec.classpath.from(classpath)
          }
        }
    workQueue.submit(TestAction::class.java) {
      it.getDestinationDirectory().set(destinationDirectory)
      it.getSource().from(source)
      it.getClasspath().from(classpath)
    }
  }
}

private class TestResult(success: Boolean, sym: Symbol.DefnSym, message: String?)

abstract class TestAction : WorkAction<TestParameter> {
  override fun execute() {
    val defaultOptions = Options.DefaultTest()
    val options =
        Options(
            defaultOptions.lib(),
            defaultOptions.debug(),
            defaultOptions.documentor(),
            defaultOptions.explain(),
            defaultOptions.json(),
            defaultOptions.progress(),
            defaultOptions.target(),
            parameters.getDestinationDirectory().get().asFile.toPath(),
            defaultOptions.test(),
            defaultOptions.threads(),
            defaultOptions.loadClassFiles(),
            defaultOptions.writeClassFiles(),
            defaultOptions.xallowredundancies(),
            defaultOptions.xlinter(),
            defaultOptions.xnoboolunification(),
            defaultOptions.xnostratifier(),
            defaultOptions.xstatistics(),
            defaultOptions.xstrictmono())

    val flix = Flix()
    parameters.getSource().asFileTree.matching { it.include("*.flix") }.forEach {
      flix.addPath(it.toPath())
    }
    parameters.getClasspath().forEach {
      when {
        it.name.endsWith(".fpkg") -> flix.addPath(it.toPath())
        it.name.endsWith(".jar") -> flix.addJar(it.toPath())
        else -> {
          // logger.debug("{} found in the compile classpath but ignored", it.toPath())
        }
      }
    }

    flix.setOptions(options)
    val context = TerminalContext.`AnsiTerminal$`.`MODULE$`
    val compileResult = flix.compile()
    when {
      compileResult.errors().isEmpty -> {
        val results: scala.collection.immutable.List<TestResult> =
            compileResult.get().tests.toList().map {
              when (it) {
                is Tuple2<*, *> -> {
                  val defn: Function0<*> = it._2() as Function0<*>
                  val sym = it._1() as Symbol.DefnSym
                  try {
                    when (val testResult = defn.apply()) {
                      is Boolean -> TestResult(testResult, sym, "Returned $testResult.")
                      else -> TestResult(true, sym, "Returned non-boolean value.")
                    }
                  } catch (ex: Exception) {
                    TestResult(false, sym, ex.message)
                  }
                }
                else -> null
              }
            } as
                scala.collection.immutable.List<TestResult>
        results.foreach { println(it) }
      }
      else -> {
        val message =
            compileResult
                .errors()
                .map { m: CompilationMessage -> m.message().fmt(context) }
                .reduce { l: String, r: String -> "$l,$r" }
        throw GradleException("Failed to compile Flix code: $message")
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
}
