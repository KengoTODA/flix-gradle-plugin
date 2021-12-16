/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import de.undercouch.gradle.tasks.download.Download
import java.util.*
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.*
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService

abstract class FlixPlugin : Plugin<Project> {
  @Inject abstract fun getJavaToolchainService(): JavaToolchainService

  private fun createFpkgTask(project: Project, sources: FileCollection): TaskProvider<Zip> {
    return project.tasks.register("fpkg", Zip::class.java) { zip ->
      zip.from(sources)
      zip.from(project.file("HISTORY.md"))
      zip.from(project.file("LICENSE.md"))
      zip.from(project.file("README.md"))

      zip.duplicatesStrategy = DuplicatesStrategy.FAIL
      zip.archiveExtension.set("fpkg")
      zip.destinationDirectory.set(project.buildDir.resolve("fpkg"))

      // for https://reproducible-builds.org/docs/jvm/
      zip.isReproducibleFileOrder = true
      zip.isPreserveFileTimestamps = false
    }
  }
  override fun apply(project: Project) {
    project.plugins.apply("java-base")
    val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java)!!

    val extension = project.extensions.create("flix", FlixExtension::class.java)
    extension.apply {
      compilerVersion.convention(loadCompilerVersion())
      sourceSets = project.objects.domainObjectContainer(FlixSourceSet::class.java)
    }

    val src =
        extension.compilerVersion.map {
          "https://github.com/flix/flix/releases/download/$it/flix.jar"
        }
    val dest = extension.compilerVersion.map { project.buildDir.resolve("flix/$it/flix.jar") }
    val downloadFlixCompiler =
        project.tasks.register("downloadFlixCompiler", Download::class.java) { download ->
          download.src(src)
          download.dest(dest)
        }

    val flixCompiler = project.configurations.create(CONFIGURATION_FOR_COMPILER)
    flixCompiler.defaultDependencies { dependencySet ->
      val dependency = project.dependencies.create(project.files(dest))
      dependencySet.add(dependency)
    }

    val mainSourceSet =
        extension.sourceSets.create("main").apply {
          source =
              project
                  .objects
                  .sourceDirectorySet("flix", "Flix main source")
                  .setSrcDirs(listOf("src/main/flix"))
          output =
              project
                  .objects
                  .directoryProperty()
                  .fileValue(project.file("${project.buildDir}/classes/flix/main"))
        }
    val testSourceSet =
        extension.sourceSets.create("test").apply {
          source =
              project
                  .objects
                  .sourceDirectorySet("flix", "Flix test source")
                  .setSrcDirs(listOf("src/test/flix"))
          output =
              project
                  .objects
                  .directoryProperty()
                  .fileValue(project.file("${project.buildDir}/classes/flix/test"))
        }
    val launcher = getJavaToolchainService().launcherFor(javaExtension.toolchain)
    val compileFlix =
        project.tasks.register(mainSourceSet.getCompileTaskName(), FlixCompile::class.java) { task
          ->
          task.dependsOn(downloadFlixCompiler)
          task.source = mainSourceSet.source
          task.destinationDirectory.set(mainSourceSet.output)
          task.launcher.set(launcher)
          flixCompiler.resolve()
          task.classpath = project.files(dest)
        }
    val testFlix =
        project.tasks.register("testFlix", FlixTest::class.java) { task ->
          task.dependsOn(downloadFlixCompiler)
          task.source = mainSourceSet.source + testSourceSet.source
          task.destinationDirectory.set(testSourceSet.output)
          task.launcher.set(launcher)
          flixCompiler.resolve()
          task.classpath = project.files(dest)
          task.report.set(
              project
                  .buildDir
                  .resolve(ReportingExtension.DEFAULT_REPORTS_DIR_NAME)
                  .resolve("flix")
                  .resolve("main.txt"))
        }
    val fpkg = createFpkgTask(project, mainSourceSet.source)
    project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME) { it.dependsOn(fpkg) }
    project.tasks.named(JavaBasePlugin.CHECK_TASK_NAME) { it.dependsOn(testFlix) }
    project.plugins.withId("java") {
      project.tasks.named(JavaPlugin.CLASSES_TASK_NAME) { it.dependsOn(compileFlix) }
      project.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java) { jar ->
        jar.from(project.objects.fileCollection().builtBy(compileFlix))
        jar.manifest.attributes["Main-Class"] = DEFAULT_MAIN_CLASS
      }
    }
    project.plugins.withId(ApplicationPlugin.APPLICATION_PLUGIN_NAME) {
      project.extensions.getByType(JavaApplication::class.java).mainClass.set(DEFAULT_MAIN_CLASS)
    }
  }

  companion object {
    const val CONFIGURATION_FOR_COMPILER = "flixCompiler"
    const val DEFAULT_MAIN_CLASS = "Main"
    private const val PROPERTIES_FILE_NAME = "flix-gradle-plugin.properties"

    fun loadCompilerVersion(): String {
      val url = FlixPlugin::class.java.classLoader.getResource(PROPERTIES_FILE_NAME)!!
      url.openStream().use {
        val prop = Properties()
        prop.load(it)
        return prop.getProperty("compiler-version")
      }
    }
  }
}
