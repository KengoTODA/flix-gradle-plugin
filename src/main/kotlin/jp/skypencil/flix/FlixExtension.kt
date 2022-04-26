/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.NamedDomainObjectContainer

/**
 * An extension to configure the flix plugin.
 *
 * @since 1.0
 */
interface FlixExtension {
  /**
   * Container of [FlixSourceSet].
   *
   * @since 1.0
   */
  var sourceSets: NamedDomainObjectContainer<FlixSourceSet>
}
