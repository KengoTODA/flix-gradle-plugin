/* (C) Kengo TODA 2021 */
package jp.skypencil.flix

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.gradle.testfixtures.ProjectBuilder

/** A simple unit test for the 'jp.skypencil.flix.greeting' plugin. */
class FlixPluginTest {
  @Test
  fun `plugin registers task`() {
    // Create a test project and apply the plugin
    val project = ProjectBuilder.builder().build()
    project.plugins.apply("jp.skypencil.flix")

    // Verify the result
    assertNotNull(project.tasks.findByName("compileFlix"))
  }

  @Test
  fun `plugin loads compiler version from a resource`() {
    assertNotNull(FlixPlugin.Companion.loadCompilerVersion())
  }
}
