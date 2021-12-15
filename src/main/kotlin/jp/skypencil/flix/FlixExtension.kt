/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaToolchainSpec

interface FlixExtension {
  val compilerVersion: Property<String>
  val jvmToolchain: JavaToolchainSpec
  var sourceSets: NamedDomainObjectContainer<FlixSourceSet>
}
