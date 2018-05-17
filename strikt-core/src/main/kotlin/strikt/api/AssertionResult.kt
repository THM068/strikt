package strikt.api

import strikt.api.Status.*
import strikt.api.reporting.Reportable

sealed class AssertionResult : Reportable {
  abstract val description: String
  abstract val status: Status
}

class PendingResult(override val description: String) : AssertionResult() {
  override val status = Pending
}

data class AtomicResult(
  override val description: String,
  override val status: Status,
  val expected: Any? = null,
  val actual: Any? = null
) : AssertionResult()

data class ComposedResult(
  override val description: String,
  override val status: Status,
  val results: Collection<AssertionResult>
) : AssertionResult() {
  val anyFailed: Boolean
    get() = results.any { it.status == Failed }

  val allFailed: Boolean
    get() = results.isNotEmpty() && results.all { it.status == Failed }

  val anyPassed: Boolean
    get() = results.any { it.status == Passed }

  val allPassed: Boolean
    get() = results.isNotEmpty() && results.all { it.status == Passed }
}
