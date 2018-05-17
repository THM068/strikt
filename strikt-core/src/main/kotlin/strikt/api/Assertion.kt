package strikt.api

import org.opentest4j.AssertionFailedError
import strikt.api.Mode.COLLECT
import strikt.api.Mode.FAIL_FAST
import strikt.api.Status.Failed
import strikt.api.Status.Passed
import kotlin.jvm.internal.CallableReference

/**
 * Holds a subject of type [T] that you can then make assertions about.
 */
class Assertion<T>
internal constructor(
  private val subject: Subject<T>,
  private val mode: Mode,
  private val negated: Boolean = false,
  private val parent: Assertion<*>? = null
) {

  val root: Assertion<*>
    get() = when (parent) {
      null -> this
      else -> parent.root
    }

  private val results = mutableListOf<AssertionResult>()

  /**
   * Evaluates a condition that may pass or fail.
   *
   * This method can be used directly in a test but is typically used inside an
   * extension method on `Assertion<T>` such as those provided in the
   * [strikt.assertions] package.
   *
   * @sample strikt.samples.AssertionMethods.assert
   *
   * @param description a description for the condition the assertion evaluates.
   * @param assertion the assertion implementation that should result in a call
   * to [AssertionContext.pass] or [AssertionContext.fail].
   * @return this assertion, in order to facilitate a fluent API.
   * @see AssertionContext.pass
   * @see AssertionContext.fail
   */
  fun assert(description: String, assertion: AssertionContext<T>.() -> Unit) =
    assert(description, null, assertion)

  fun assert(description: String, expected: Any?, assertion: AssertionContext<T>.() -> Unit) =
    apply {
      AssertionContextImpl(this, description, expected).apply(assertion).result.let {
        results.add(it)
        if (it.status == Failed && mode == FAIL_FAST) {
          TODO("throw ${AssertionFailedError::class.java.simpleName}")
        }
      }
    }

  /**
   * Evaluates a boolean condition.
   * This is useful for implementing the simplest types of assertion function.
   *
   * @param description a description for the condition the assertion evaluates.
   * @param assertion a function that returns `true` (the assertion passes) or
   * `false` (the assertion fails).
   * @return this assertion, in order to facilitate a fluent API.
   */
  // TODO: this name sucks
  fun passesIf(description: String, assertion: T.() -> Boolean) =
    passesIf(description, null, assertion)

  fun passesIf(description: String, expected: Any?, assertion: T.() -> Boolean) =
    apply {
      assert(description, expected) {
        if (subject.assertion()) pass() else fail()
      }
    }

  /**
   * Maps the assertion subject to the result of [function].
   * This is useful for chaining to property values or method call results on
   * the subject.
   *
   * If [function] is a callable reference, (for example a getter or property
   * reference) the subject description will be automatically determined for the
   * returned assertion.
   *
   * @sample strikt.samples.AssertionMethods.map
   *
   * @param function a lambda whose receiver is the current assertion subject.
   * @return an assertion whose subject is the value returned by [function].
   */
  // TODO: not sure about this name, it's fundamentally similar to Kotlin's run. Also it might be nice to have a dedicated `map` for Assertion<Iterable>.
  fun <R> map(function: T.() -> R): Assertion<R> =
    when (function) {
      is CallableReference -> map(".${function.propertyName} %s", function)
      else                 -> map("%s", function)
    }

  /**
   * Maps the assertion subject to the result of [function].
   * This is useful for chaining to property values or method call results on
   * the subject.
   *
   * @sample strikt.samples.AssertionMethods.map
   *
   * @param description a description of the mapped result.
   * @param function a lambda whose receiver is the current assertion subject.
   * @return an assertion whose subject is the value returned by [function].
   */
  fun <R> map(description: String, function: T.() -> R): Assertion<R> =
    Subject(description, subject.value.function())
      .let { Assertion(it, mode, negated, this) }

  /**
   * Reverses any assertions chained after this method.
   *
   * @sample strikt.samples.AssertionMethods.not
   *
   * @return an assertion that negates the results of any assertions applied to
   * its subject.
   */
  fun not(): Assertion<T> = Assertion(subject, mode, !negated, this)

  // TODO: if we could make this a data class we could just use copy
  internal fun descendant() = Assertion(subject, mode, negated, this)

  val allPassed: Boolean
    get() = results.all { it.status == Passed }

  val anyPassed: Boolean
    get() = results.any { it.status == Passed }

  val allFailed: Boolean
    get() = results.all { it.status == Failed }

  val anyFailed: Boolean
    get() = results.any { it.status == Failed }

  private val CallableReference.propertyName: String
    get() = "^get(.+)$".toRegex().find(name).let { match ->
      return when (match) {
        null -> name
        else -> match.groupValues[1].decapitalize()
      }
    }

  // TODO: rename
  private class AssertionContextImpl<T>(
    private val assertion: Assertion<T>,
    private val description: String,
    private val expected: Any?
  ) : AssertionContext<T> {
    internal var result: AssertionResult = PendingResult(description)

    override val subject = assertion.subject.value

    override fun pass() {
      result = if (assertion.negated) {
        AtomicResult(description, Failed, expected)
      } else {
        AtomicResult(description, Passed)
      }
    }

    override fun fail() {
      result = if (assertion.negated) {
        AtomicResult(description, Passed)
      } else {
        AtomicResult(description, Failed, expected)
      }
    }

    override fun fail(actual: Any?) {
      result = if (assertion.negated) {
        AtomicResult(description, Passed)
      } else {
        AtomicResult(description, Failed, expected, actual)
      }
    }

    override fun compose(assertions: ComposedAssertions<T>.() -> Unit): ComposedAssertionContext =
      ComposedAssertionsImpl(assertion, description).apply(assertions)
  }

  private class ComposedAssertionsImpl<T>(
    private val assertion: Assertion<T>,
    private val description: String
  ) : ComposedAssertionContext, ComposedAssertions<T> {

    private var result: AssertionResult = PendingResult(description)
    private val results = mutableListOf<AssertionResult>()

    override fun pass() {
      result = if (assertion.negated) {
        ComposedResult(description, Failed, results)
      } else {
        ComposedResult(description, Passed, results)
      }
    }

    override fun fail() {
      result = if (assertion.negated) {
        ComposedResult(description, Passed, results)
      } else {
        ComposedResult(description, Failed, results)
      }
    }

    // TODO: there's an interface in common between this class and Assertion
    override val allPassed: Boolean
      get() = results.all { it.status == Passed }

    override val anyPassed: Boolean
      get() = results.any { it.status == Passed }

    override val allFailed: Boolean
      get() = results.all { it.status == Failed }

    override val anyFailed: Boolean
      get() = results.any { it.status == Failed }

    override fun <E> expect(description: String, subject: E): Assertion<E> =
      Subject(description, subject)
        .let { Assertion(it, COLLECT, assertion.negated, assertion) }

    override fun <E> expect(
      description: String,
      subject: E,
      block: Assertion<E>.() -> Unit
    ): Assertion<E> =
      Subject(description, subject)
        .let { Assertion(it, COLLECT, assertion.negated, assertion).apply(block) }

    override fun assert(description: String, block: AssertionContext<T>.() -> Unit) =
      assertion.descendant().assert(description, block)
  }
}
