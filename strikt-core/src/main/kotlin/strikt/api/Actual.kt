package strikt.api

data class Actual<T>(
  val description: String,
  val value: T
) {
  override fun toString() = String.format(description, value)
}