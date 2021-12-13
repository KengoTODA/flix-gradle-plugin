/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.file.SourceDirectorySet

abstract class FlixSourceSet(name: String) {
  val name = name

  lateinit var source: SourceDirectorySet

  lateinit var resources: SourceDirectorySet

  private fun toUpperCamel(string: String): String {
    return StringBuilder().append(string[0].uppercaseChar()).append(string.substring(1)).toString()
  }

  fun getCompileTaskName(): String {
    return when (name) {
      "main" -> "compileFlix"
      else -> "compile${toUpperCamel(name)}Flix"
    }
  }
}
