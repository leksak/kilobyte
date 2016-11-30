package decompiler;

import com.google.common.base.Preconditions.checkArgument
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.Reader
import java.util.*

object MachineCodeDecoder {
  @JvmStatic fun decode(f: File): List<Long> {
    return decode(FileReader(f))
  }

  @JvmStatic fun decode(reader: Reader): List<Long> {
    val br = BufferedReader(reader)

    val instructions = ArrayList<Long>()
    var line: String? = br.readLine()
    while (line != null) {
      if (line.isEmpty()) {
        continue
      }

      instructions.add(decode(line))
      line = br.readLine()
    }

    return instructions
  }

  @JvmStatic fun equals(s1: String, s2: String): Boolean {
    return decode(s1) == decode(s2)
  }

  @JvmStatic fun decode(str: String): Long {
    var s = str
    checkArgument(s.isNotEmpty(), "Expected input argument to have non-zero length")
    s = s.trim { it <= ' ' } // Remove trailing and leading spaces

    // Convert the string to lower-case, simplifies regex validating
    // the String format as well as matching on the prefix.
    s = s.toLowerCase()

    /*
     * Check that all characters match either hexadecimal, decimal
     * or binary strings.
     */
    if (!s.matches("-?([0-9]+)|(0[x][0-9a-f]+)|(0[b][0-1]+)|(0[d])?[0-9]+".toRegex())) {
      throw NumberFormatException("Could not interpret \"$str\" as a number")
    }

    if (s.length <= 1) {
      // There can be no specified prefix
      return s.toLong()
    }
    // Yank out the two leading characters
    val prefix = s.substring(0, 2)

    // Assuming that the number is not prefixed and then we default
    // to interpreting the number as being a decimal number (base 10)
    var base = 10
    var encounteredPrefix = false

    // The earlier .toLowerCase call allows us to switch on lower-case
    // string representations.
    when (prefix) {
      "0x" -> {
        base = 16
        encounteredPrefix = true
      }
      "0b" -> {
        base = 2
        encounteredPrefix = true
      }
      "0d" -> encounteredPrefix = true
      else -> {
      }
    }// Our regex assures us that the two characters
    // that we have can only be numbers between 0 and 9
    // so treating the string as being in base 10 is
    // safe from here on.

    if (encounteredPrefix) {
      // Remove the prefix from the string for the subsequent parseInt
      // call. The method call "substring(2)" splits the string at
      // the second character (exclusive) so we get that
      //
      // "0x014b4820" becomes "014b4820"
      //
      // Remove the prefix from the string, substring() takes the
      // tail starting _behind_ the second character.
      s = s.substring(2)
    }
    if (s.startsWith('-')) {
      return Integer.parseInt(s, base).toLong()
    }

    // parseUnsignedInt lets us handle "large" numbers such as 0xafbf0004
    return Integer.parseUnsignedInt(s, base).toLong()

  }

  @JvmStatic fun decode(s: String, base: Int): Int {
    return Integer.parseInt(decode(s).toString(), base)
  }

  @Throws(NumberFormatException::class)
  @JvmStatic fun decode(numbers: List<String>) : List<Long> {
    val successfullyDecoded : MutableList<Long> = ArrayList()
    numbers.map { it -> try {
      successfullyDecoded.add(decode(it))
    } catch (e: NumberFormatException) {
      println("Failed to decode \"$it\". Cause: ${e.message}")
    }
    }
    return successfullyDecoded
  }
}
