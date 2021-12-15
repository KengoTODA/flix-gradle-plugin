/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

interface FlixExtension {
  val compilerVersion: Property<String>
  var sourceSets: NamedDomainObjectContainer<FlixSourceSet>
}
