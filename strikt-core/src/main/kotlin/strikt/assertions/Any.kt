package strikt.assertions

import strikt.api.Assertion

/**
 * Asserts that the subject is `null`.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Assertion<T?>.isNull(): Assertion<Nothing> =
  assert("is null") {
    when (subject) {
      null -> pass()
      else -> fail()
    }
  } as Assertion<Nothing>

/**
 * Asserts that the subject is not `null`.
 *
 * @return an assertion for a non-`null` subject.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Assertion<T?>.isNotNull(): Assertion<T> =
  assert("is not null") {
    when (subject) {
      null -> fail()
      else -> pass()
    }
  } as Assertion<T>

/**
 * Asserts that the subject is an instance of [T].
 *
 * @return an assertion for [T].
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Assertion<*>.isA(): Assertion<T> =
  assert("is an instance of %s", T::class.java.name) {
    when (subject) {
      null -> fail(null)
      is T -> pass()
      else -> fail(subject!!.javaClass.name)
    }
  } as Assertion<T>

/**
 * Asserts that the subject is equal to [expected] according to the standard
 * Kotlin `==` operator.
 *
 * @param expected the expected value.
 */
fun <T> Assertion<T>.isEqualTo(expected: Any?): Assertion<T> =
  assert("is equal to $expected") {
    when (subject) {
      expected -> pass()
      else     -> fail()
    }
  }

/**
 * Asserts that the subject is not equal to [expected] according to the standard
 * Kotlin `==` operator.
 *
 * @param expected the expected value.
 */
fun <T> Assertion<T>.isNotEqualTo(expected: Any?): Assertion<T> =
  assert("is equal to $expected") {
    when (subject) {
      expected -> fail()
      else     -> pass()
    }
  }
