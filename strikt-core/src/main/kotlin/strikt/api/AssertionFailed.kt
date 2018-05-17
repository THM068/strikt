package strikt.api

import org.opentest4j.AssertionFailedError
import strikt.api.reporting.writeToString

/**
 * Thrown to indicate an assertion, or a group or chain of assertions has
 * failed.
 *
 * @property message the human-readable assertion description.
 */
class AssertionFailed
internal constructor(
  val subject: AssertionResult
) : AssertionFailedError() {
  override val message: String by lazy { subject.writeToString() }
}
