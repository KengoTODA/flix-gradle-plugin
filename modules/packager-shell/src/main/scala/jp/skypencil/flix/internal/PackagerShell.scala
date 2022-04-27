/* (C) Kengo TODA 2022 */
package jp.skypencil.flix.internal;

import java.nio.file.Path;
import ca.uwaterloo.flix.util.Options;
import ca.uwaterloo.flix.tools.Tester.TestResults
import ca.uwaterloo.flix.tools.Tester.OverallTestResult.Failure

/** A thin shell wrapping the Flix API, to ease invoking API written in Scala
  * from Kotlin code.
  */
object PackagerShell {
  def createOptions(output: Path): Options = Options.Default.copy(
    output = Some(output)
  );
  def hasTestFailure(results: TestResults): Boolean =
    results.overallResult match {
      case Failure => true
      case _       => false
    }
}
