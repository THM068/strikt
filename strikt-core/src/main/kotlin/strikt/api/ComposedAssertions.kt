package strikt.api

/**
 * Allows assertions to be composed, or nested.
 * This class is the receiver of the lambda passed to
 * [AssertionContext.compose].
 */
interface ComposedAssertions<T> {
  /**
   * Start a chain of assertions in the current nested context.
   *
   * @param subject the subject of the chain of assertions, usually a property
   * or element of the subject of the surrounding assertion.
   * @return an assertion for [subject].
   */
  fun <E> expect(subject: E): Assertion<E> = expect("Expect that %s", subject)

  /**
   * Start a chain of assertions in the current nested context.
   *
   * @param description a description for [subject] with a [String.format] style
   * placeholder for the value itself.
   * @param subject the subject of the chain of assertions, usually a property
   * or element of the subject of the surrounding assertion.
   * @return an assertion for [subject].
   */
  fun <E> expect(description: String, subject: E): Assertion<E>

  /**
   * Evaluate a block of assertions in the current nested context.
   *
   * @param subject the subject of the block of assertions, usually a property
   * or element of the subject of the surrounding assertion.
   * @param block a closure that can perform multiple assertions that will all
   * be evaluated regardless of whether preceding ones pass or fail.
   * @return an assertion for [subject].
   */
  fun <E> expect(subject: E, block: Assertion<E>.() -> Unit): Assertion<E> =
    expect("Expect that %s", subject, block)

  /**
   * Evaluate a block of assertions in the current nested context.
   *
   * @param description a description for [subject] with a [String.format] style
   * placeholder for the value itself.
   * @param subject the subject of the block of assertions, usually a property
   * or element of the subject of the surrounding assertion.
   * @param block a closure that can perform multiple assertions that will all
   * be evaluated regardless of whether preceding ones pass or fail.
   * @return an assertion for [subject].
   */
  fun <E> expect(
    description: String,
    subject: E,
    block: Assertion<E>.() -> Unit
  ): Assertion<E>

  /**
   * Evaluates a composed assertion on the original subject.
   * This creates a new assertion in the composed context using the same
   * subject as the overall assertion.
   * This is useful because it allows for the overall assertion to contain much
   * more detail in any failure message.
   */
  fun assert(description: String, block: AssertionContext<T>.() -> Unit): Assertion<T>
}
