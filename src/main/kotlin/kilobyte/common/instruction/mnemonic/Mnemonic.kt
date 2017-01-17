package kilobyte.common.instruction.mnemonic

import kilobyte.common.extensions.*
import kilobyte.common.hardware.Register
import kilobyte.common.instruction.exceptions.IllegalCharactersInMnemonicException
import kilobyte.common.instruction.exceptions.MalformedMnemonicException
import java.util.*
import java.util.regex.Pattern

/**
 * Will compare two given Strings that will be compared formatting. Since the
 * mnemonic do not need to be identical to be equal they need a more excessive
 * comparision.
 *
 * For example the Register '$a0' and '$4' is equal. Also can formatting of
 * numbers be done in different bases such as hexadecimal or decimal.
 */
fun mnemonicEquals(s1: String, s2: String): Boolean {
  val standard1 = standardizeMnemonic(s1)
  val standard2 = standardizeMnemonic(s2)

  if (standard1 == standard2) return true

  val tokens1 = standard1.tokenize()
  val tokens2 = standard2.tokenize()
  if (tokens1.size != tokens2.size) {
    return false
  }
  for (i in 0..tokens1.size - 1) {
    val tok1 = tokens1[i]
    val tok2 = tokens2[i]

    // If the strings are equal then we can move on to the next token
    if (tok1 == tok2) continue

    // Are they both registers?
    val isReg1 = tok1.startsWith("\$")
    val isReg2 = tok2.startsWith("\$")

    // Is one a register but the other one isn't?
    // If so, then the mnemonics are not equal
    if (isReg1 xor isReg2) return false

    // Are both registers?
    if (isReg1 and isReg2) {
      // Are they the same register? If not the mnemonics are not equal
      if (!Register.equals(tok1, tok2)) {
        return false
      }
      continue
    } else {
      // Either we are looking at a "pure" number (but the two tokens may be in different bases
      // such as tok1=0x01 and tok2=1 or we may have tokens on the form "4($t0)", where
      // one token is "0x04($t0)" and the other "4($8)"
      if (tok1.contains('(') and tok2.contains('(')) {
        // Both strings are on the form "offset(reg)"
        val offset1 = tok1.getOffset() // getOffset handles translating bases
        val offset2 = tok2.getOffset()
        if (offset1 != offset2) return false

        val reg1 = tok1.getRegister() // Handles symbolic difference like $t0 and $t8
        val reg2 = tok2.getRegister()
        if (reg1 != reg2) return false
      }
    }
  }
  return true
}

fun String.iname(): String = this.tokenize()[0]

fun throwExceptionIfContainsIllegalCharacters(standardizedMnemonic: String) {
  // Throw an exception if the passed string contains a new line character
  if (standardizedMnemonic.containsNewlineCharacter()) {
    throw
    IllegalCharactersInMnemonicException(standardizedMnemonic, "<newline>")
  }

  // Check for other illegal characters:
  // goo.gl/Q8EiLb.
  //
  // We only consider letters, commas, spaces, numbers,
  // dollar signs and parentheses as being legal characters.
  val regex = "[A-Za-z, \\-()0-9\$]"
  val p = Pattern.compile(regex)
  val matcher = p.matcher(standardizedMnemonic)

  // Given the input
  //
  // add $t1, $t2, $t3!#$sp
  //
  // then the below statement will yield a mask with "+" signs on characters
  // that match the regex and "^" signs on characters that do not
  // match, i.e. illegal characters. So, for the above example the
  // match will be
  //
  // mask == +++++++++++++++++^^+++
  val mask = matcher.replaceAll("+").replace(Regex("[^+]"), "^")
  if (mask.contains("^")) {
    // At least one illegal character was detected,
    val illegalCharacters = StringJoiner("', '", "['", "']")
    for (i in 0..(standardizedMnemonic.length - 1)) {
      if (mask[i] == '^') {
        illegalCharacters.add(standardizedMnemonic[i].toString())
      }
    }
    throw IllegalCharactersInMnemonicException(
          standardizedMnemonic, illegalCharacters)
  }
}

/**
 * Check if given String contains the excepted number of commas.
 *
 * Example(s):
 * add $t1, $t2, $t3    (2)
 * jr $t1               (0)
 * break                (0)
 */
fun throwIfIncorrectNumberOfCommas(expectedNumberOfCommas: Int, standardizedMnemonic: String) {
  val actualNumberOfCommas = standardizedMnemonic.countCommas()

  if (actualNumberOfCommas != expectedNumberOfCommas) {
    val expected = expectedNumberOfCommas
    val actual = actualNumberOfCommas
    val err = "Wrong number of commas: Expected: $expected. Got: $actual"
    throw MalformedMnemonicException(standardizedMnemonic, err)
  }
}

/**
 * Replace all white-space characters (\\s+) with a single space and
 * will ensure that any commas in the return value are always followed
 * by a single space. Any leading or trailing spaces are removed (trimmed)
 * from the return value.
 *
 * Examples:
 * add $t1,$t2, $t3       (intentional space before $t3)
 * add $t1, $t2,   $t3    (triple space before $t3)
 * add $t1, $t2, $t3      (Correct String)
 * will all be formatted to:
 * add $t1, $t2, $t3
 */
fun standardizeMnemonic(mnemonic: String): String {
  return mnemonic.replace(",", ", ").replace(Regex("\\s+"), " ").trim()
}