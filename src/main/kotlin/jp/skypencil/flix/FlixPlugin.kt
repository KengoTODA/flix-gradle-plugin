/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import de.undercouch.gradle.tasks.download.Download
import java.util.Properties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

class FlixPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply("java-base")
    val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java)!!

    val extension = project.extensions.create("flix", FlixExtension::class.java)
    extension.apply {
      compilerVersion.convention(loadCompilerVersion())
      jvmToolchain.convention(project.provider { javaExtension.toolchain })
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
          resources =
              project
                  .objects
                  .sourceDirectorySet("resources", "Flix main resource")
                  .setSrcDirs(listOf("src/main/resources"))
          output =
              project
                  .objects
                  .directoryProperty()
                  .fileValue(project.file("${project.buildDir}/classes/flix/main"))
        }
    val compileFlix =
        project.tasks.register(mainSourceSet.getCompileTaskName(), FlixCompile::class.java) { task
          ->
          task.dependsOn(downloadFlixCompiler)
          task.source = mainSourceSet.source
          task.destinationDirectory.set(mainSourceSet.output)
          flixCompiler.resolve()
          task.classpath = project.files(dest)
        }
    project.plugins.withId("java") {
      project.tasks.named(JavaPlugin.CLASSES_TASK_NAME) { it.dependsOn(compileFlix) }
      project.tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class.java) { jar ->
        jar.from(mainSourceSet.output)
      }
    }
  }

  companion object {
    const val CONFIGURATION_FOR_COMPILER = "flixCompiler"
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
