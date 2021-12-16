/* (C) Kengo TODA 2021 */
package jp.skypencil.flix.internal

import javax.inject.Inject
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

abstract class WorkQueueFactory(private val logger: Logger) {
  @Inject abstract fun getWorkerExecutor(): WorkerExecutor

  fun createWorkQueue(launcher: Property<JavaLauncher>, classpath: FileCollection): WorkQueue {
    return if (launcher.isPresent) {
      val launcherPath = launcher.get().executablePath
      logger.debug(
          "Java toolchain found, using the following launcher to run Flix compiler: {}",
          launcherPath)
      getWorkerExecutor().processIsolation { workerSpec ->
        workerSpec.classpath.from(classpath)
        workerSpec.forkOptions.setExecutable(launcherPath)
      }
    } else {
      logger.debug("No Java toolchain found, using the class loader isolation mode.")
      getWorkerExecutor().classLoaderIsolation { workerSpec ->
        workerSpec.classpath.from(classpath)
      }
    }
  }
}
