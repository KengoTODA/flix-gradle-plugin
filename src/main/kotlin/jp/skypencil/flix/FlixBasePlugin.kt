/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import de.undercouch.gradle.tasks.download.Download
import java.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class FlixBasePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply("java-base")

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
        project.tasks.register(DOWNLOAD_COMPILER_TASK_NAME, Download::class.java) { it ->
          it.src(src)
          it.dest(dest)
        }
    project.tasks.withType(FlixCompile::class.java) {
      it.classpath = project.files(dest).builtBy(downloadFlixCompiler)
    }
    project.tasks.withType(FlixTest::class.java) {
      it.classpath = project.files(dest).builtBy(downloadFlixCompiler)
    }
    val flixCompiler = project.configurations.create(CONFIGURATION_FOR_COMPILER)
    flixCompiler.defaultDependencies { dependencySet ->
      val dependency = project.dependencies.create(project.files(dest))
      dependencySet.add(dependency)
    }
  }

  companion object {
    const val CONFIGURATION_FOR_COMPILER = "flixCompiler"
    const val DOWNLOAD_COMPILER_TASK_NAME = "downloadFlixCompiler"
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