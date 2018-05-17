package strikt.api.reporting

import strikt.api.AssertionResult
import strikt.api.AtomicResult
import strikt.api.ComposedResult
import strikt.api.Status.*
import strikt.api.Subject

internal open class DefaultResultWriter : ResultWriter {
  override fun writeTo(writer: Appendable, result: Reportable) {
    writeIndented(writer, result)
  }

  private fun writeIndented(writer: Appendable, result: Reportable, indent: Int = 0) {
    writeLine(writer, result, indent)
    if (result is ComposedResult) {
      result.results.forEach {
        writeIndented(writer, it, indent + 1)
      }
    }
  }

  protected open fun writeLine(writer: Appendable, result: Reportable, indent: Int) {
    when (result) {
      is Subject<*>      -> result.writeSubject(writer, indent)
      is AssertionResult -> result.writeResult(writer, indent)
    }
  }

  private fun Subject<*>.writeSubject(writer: Appendable, indent: Int) {
    writeLineStart(writer, this, indent)
    writeSubjectIcon(writer)
    // TODO: handle without String.format
    writer.append(description.format(formatValue(value)))
    writeLineEnd(writer, this)
  }

  private fun AssertionResult.writeResult(writer: Appendable, indent: Int) {
    writeLineStart(writer, this, indent)
    writeStatusIcon(writer, this)
    writer.append(description)
    writeLineEnd(writer, this)

    // TODO: recurse here, Actual should be just another Reportable
    if (this is AtomicResult) {
      writeActual(writer, indent)
    }
  }

  private fun AtomicResult.writeActual(writer: Appendable, indent: Int) {
    actual?.let { actual ->
      writeLineStart(writer, this, indent + 1)
      writeActualValueIcon(writer)
      // TODO: handle without String.format
      writer.append("found %s".format(formatValue(actual)))
      writeLineEnd(writer, this)
    }
  }

  protected open fun writeLineStart(writer: Appendable, node: Reportable, indent: Int) {
    writer.append("".padStart(2 * indent))
  }

  protected open fun writeLineEnd(writer: Appendable, node: Reportable) {
    writer.append("\n")
  }

  protected open fun writeStatusIcon(writer: Appendable, node: Reportable) {
    if (node is AssertionResult) {
      writer.append(when (node.status) {
        Passed  -> "✓ "
        Failed  -> "✗ "
        Pending -> "? "
      })
    }
  }

  protected open fun writeActualValueIcon(writer: Appendable) {
    writer.append("• ")
  }

  protected open fun writeSubjectIcon(writer: Appendable) {
    writer.append("▼ ")
  }

  protected open fun formatValue(value: Any?): Any =
    when (value) {
      null            -> "null"
      is CharSequence -> "\"$value\""
      is Char         -> "'$value'"
      is Iterable<*>  -> value.map(this::formatValue)
      is Class<*>     -> value.name
      else            -> value
    }
}
