/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.SourceDirectorySet

abstract class FlixSourceSet(val name: String) {

  lateinit var source: SourceDirectorySet

  lateinit var output: DirectoryProperty

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
