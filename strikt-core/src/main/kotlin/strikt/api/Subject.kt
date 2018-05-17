package strikt.api

import strikt.api.reporting.Reportable

data class Subject<T>(
  val description: String,
  val value: T
) : Reportable