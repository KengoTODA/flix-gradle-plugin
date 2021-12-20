/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

/**
 * An extension to configure the flix plugin.
 *
 * @since 1.0
 */
interface FlixExtension {
  /**
   * Version of compiler distributed at the
   * [GitHub Releases](https://github.com/flix/flix/releases/).
   *
   * @since 1.0
   */
  val compilerVersion: Property<String>

  /**
   * Container of [FlixSourceSet].
   *
   * @since 1.0
   */
  var sourceSets: NamedDomainObjectContainer<FlixSourceSet>
}
