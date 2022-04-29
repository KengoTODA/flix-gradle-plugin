/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.language.CompilationMessage
import javax.inject.Inject
import jp.skypencil.flix.internal.`PackagerShell$`
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

/**
 * Task to compile Flix code into class files.
 *
 * Here is an example to register a task to compile Flix code located in the `src` directory:
 *
 * ```kotlin
 * tasks.register<FlixCompile>("compileFlix") {
 *   source = fileTree("src")
 *   destinationDirectory.set(file("build/classes/flix/main"))
 * }
 * ```
 *
 * @since 1.0
 */
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
    val options =
        `PackagerShell$`.`MODULE$`.createOptions(
            parameters.getDestinationDirectory().get().asFile.toPath())

    val flix = Flix()
    parameters
        .getSource()
        .asFileTree
        .matching { it.include("*.flix") }
        .forEach { flix.addSourcePath(it.toPath()) }
    parameters.getClasspath().forEach {
      when {
        it.name.endsWith(".fpkg") -> flix.addSourcePath(it.toPath())
        it.name.endsWith(".jar") -> flix.addJar(it.toPath())
        else -> {
          System.err.printf("%s found in the compile classpath but ignored", it.toPath())
        }
      }
    }

    flix.setOptions(options)
    val compileResult = flix.compile()
    when {
      compileResult.errors().isEmpty -> {
        System.err.println("Flix code has been compiled successfully.")
      }
      else -> {
        val message =
            compileResult
                .errors()
                .map { m: CompilationMessage -> m.message(flix.formatter) }
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
