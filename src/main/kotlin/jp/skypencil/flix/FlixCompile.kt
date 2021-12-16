/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.language.CompilationMessage
import ca.uwaterloo.flix.util.Options
import ca.uwaterloo.flix.util.vt.TerminalContext
import javax.inject.Inject
import jp.skypencil.flix.internal.WorkQueueFactory
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

@CacheableTask
abstract class FlixCompile : AbstractCompile() {
  @get:Nested
  @get:Optional
  val launcher: Property<JavaLauncher> = project.objects.property(JavaLauncher::class.java)

  @Inject abstract fun getWorkerExecutor(): WorkerExecutor

  private val workQueueFactory: WorkQueueFactory = WorkQueueFactory(logger, getWorkerExecutor())

  @TaskAction
  fun run() {
    val workQueue = workQueueFactory.createWorkQueue(launcher, classpath)
    workQueue.submit(CompileAction::class.java) {
      it.getDestinationDirectory().set(destinationDirectory)
      it.getSource().from(source)
      it.getClasspath().from(classpath)
    }
  }
}

abstract class CompileAction : WorkAction<CompileParameter> {
  override fun execute() {
    val defaultOptions = Options.Default()
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
          System.err.printf("%s found in the compile classpath but ignored", it.toPath())
        }
      }
    }

    flix.setOptions(options)
    val context = TerminalContext.`AnsiTerminal$`.`MODULE$`
    val compileResult = flix.compile()
    when {
      compileResult.errors().isEmpty -> {
        System.err.println("Flix code has been compiled successfully.")
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

interface CompileParameter : WorkParameters {
  // TODO Cannot pass ConfigurableFileTree, so using FileCollection as workaround
  // https://github.com/gradle/gradle/issues/18770
  // https://github.com/gradle/gradle/issues/19174
  fun getSource(): ConfigurableFileCollection
  fun getClasspath(): ConfigurableFileCollection
  fun getDestinationDirectory(): DirectoryProperty
}
