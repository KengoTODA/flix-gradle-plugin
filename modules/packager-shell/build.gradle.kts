plugins {
  `convention-plugin`
  scala
}

group = "jp.skypencil.flix"

repositories { mavenCentral() }

dependencies {
  compileOnly(tasks.downloadFlixCompiler.map { it.outputs.files })
  implementation("org.scala-lang:scala3-library_3:3.2.1")
}
