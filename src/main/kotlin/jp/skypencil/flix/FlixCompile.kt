/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import ca.uwaterloo.flix.api.Flix
import ca.uwaterloo.flix.util.Options
import ca.uwaterloo.flix.util.vt.TerminalContext
import org.gradle.api.GradleException
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile

@CacheableTask
abstract class FlixCompile() : AbstractCompile() {
  @TaskAction
  fun run() {
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
            destinationDirectory.get().asFile.toPath(),
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
    this.source.matching { it.include("*.flix") }.forEach { flix.addPath(it.toPath()) }
    this.classpath.forEach {
      when {
        it.name.endsWith(".fpkg") -> flix.addPath(it.toPath())
        it.name.endsWith(".jar") -> flix.addJar(it.toPath())
        else -> {
          logger.debug("{} found in the compile classpath but ignored", it.toPath())
        }
      }
    }

    // TODO put downloaded jar file to the classpath when launch a worker
    // val flixCompiler = project.configurations.getByName(FlixPlugin.CONFIGURATION_FOR_COMPILER)
    // project.files(flixCompiler)

    // TODO apply the Worker API
    // TODO hack Packager to stop throwing "does not appear to be a flix project" error

    flix.setOptions(options)
    val context = TerminalContext.`AnsiTerminal$`.`MODULE$`
    val compileResult = flix.compile()
    when {
      compileResult.errors().isEmpty -> {
        logger.debug("Flix code has been compiled successfully.")
      }
      else -> {
        compileResult.errors().foreach { logger.error(it.message().fmt(context)) }
        throw GradleException("Failed to compile Flix code")
      }
    }
  }
}
