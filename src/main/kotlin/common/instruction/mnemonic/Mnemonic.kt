package common.instruction.mnemonic

import common.hardware.Register
import common.instruction.exceptions.IllegalCharactersInMnemonicException
import common.instruction.exceptions.MalformedMnemonicException
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
  val tokens1 = s1.tokenize()
  val tokens2 = s2.tokenize()
  if (tokens1.size != tokens2.size) {
    return false
  }
  for (i in 0..tokens1.size - 1) {
    val tok1 = tokens1[i]
    val tok2 = tokens2[i]

    val tok1DenotesRegister = tok1.startsWith("\$")
    val tok2DenotesRegister = tok2.startsWith("\$")

    // Both have to either denote a register or not simultaneously
    if (tok1DenotesRegister && tok2DenotesRegister) {
      // Both denote a register, do they denote the same register?
      if (!Register.equals(tok1, tok2)) { return false }
    } else {
      // Either one denotes a register and the other does not or
      // they are both not registers.
      if (tok1DenotesRegister.xor(tok2DenotesRegister)) {
        // True if one is a register and the other is not hence the
        // two representations are not equal!
        return false
      }

      if (tok1 != tok2) {
        // The tokens isn't register nor equal, however they can be
        // address-format equal.
        //TODO: Will this be too arbitrary?
        try {
          if (Register.offsetFromOffset(tok1) == Register.offsetFromOffset(tok1)) {
            if (Register.registerFromOffset(tok1) == Register.registerFromOffset(tok1)) {
              //The tokens is address and equal.
              continue
            }
          }
        } catch (e: IllegalArgumentException) {
          //Since it is not a number to start with, it cant be number equal
          // or a address-String.
          return false
        } catch (e : StringIndexOutOfBoundsException) {
          //this means that the string only was different numeric-base.
          continue
        }
        // Neither string is a register so compare them for simple equality
        return false
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