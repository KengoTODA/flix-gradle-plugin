/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

class FlixPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.apply("java-base")
    val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java)!!

    val extension = project.extensions.create("flix", FlixExtension::class.java)
    extension.apply {
      compilerVersion.convention("v0.25.0")
      jvmToolchain.convention(project.provider { javaExtension.toolchain })
      sourceSets = project.objects.domainObjectContainer(FlixSourceSet::class.java)
    }

    val flixCompiler = project.configurations.create(CONFIGURATION_FOR_COMPILER)
    flixCompiler.defaultDependencies { dependencySet ->
      val dependency =
          project.dependencies.create(
              extension.compilerVersion.map {
                "https://github.com/flix/flix/releases/download/${ it }/flix.jar"
              })
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
        }
    project.tasks.register(mainSourceSet.getCompileTaskName(), FlixCompile::class.java) { task ->
      task.source = mainSourceSet.source
      task.destinationDirectory.set(project.file("${project.buildDir}/flix/${task.name}"))
      // TODO configure classpath
      task.classpath = project.objects.fileCollection()
      // TODO support java toolchain
      // task.jvmToolchain.convention(extension.jvmToolchain)
    }
  }

  companion object {
    const val CONFIGURATION_FOR_COMPILER = "flixCompiler"
  }
}
