package strikt.assertions

import strikt.api.Assertion

inline fun <reified E : Throwable> Assertion<() -> Unit>.throws(): Assertion<E> {
  var exception: E? = null
  assert("throws ${E::class.java.article} %s", E::class.java) {
    val caught = try {
      subject()
      null
    } catch (e: Throwable) {
      e
    }
    when (caught) {
      null -> fail(null) // TODO: message ok here?
      is E -> {
        pass()
        exception = caught
      }
      else -> fail(caught.javaClass)
    }
  }
  return if (exception != null) map { exception!! } else throw IllegalStateException()
}

val Class<*>.article: String
  get() = if (simpleName.contains("^[AEIOU]".toRegex())) "an" else "a"