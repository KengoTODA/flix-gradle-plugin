/* (C) Kengo TODA 2022 */
package jp.skypencil.flix.internal;

import java.nio.file.Path;
import ca.uwaterloo.flix.api.Flix;
import ca.uwaterloo.flix.language.CompilationMessage;
import ca.uwaterloo.flix.util.Options;
import ca.uwaterloo.flix.util.vt.TerminalContext;

/** A thin shell wrapping the Flix API, to ease invoking API written in Scala
  * from Kotlin code.
  */
object PackagerShell {
  def createOptions(targetDirectory: Path): Options = Options.Default.copy(
    targetDirectory = targetDirectory
  );
}
