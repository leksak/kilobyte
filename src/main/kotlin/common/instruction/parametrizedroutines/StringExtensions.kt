package common.instruction.parametrizedroutines

import common.hardware.Register
import common.instruction.Format
import common.instruction.exceptions.IllegalCharactersInMnemonicException
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern

/**
 * Returns true of the given String contains parentheses.
 */
fun String.containsParentheses() = this.matches(Regex(".*[()].*"))

/**
 * Returns the number of commas of given String.
 */
fun String.countCommas(): Int = StringUtils.countMatches(this, ",")

/**
 * Returns true if given String contains a newline-character.
 */
fun String.containsNewlineCharacter(): Boolean = {
  this.contains(System.getProperty("line.separator"))
}.invoke()

/**
 * Will tokenize a mnemonic String so it can return an array of tokens.
 * Produces an array of the tokens in the pattern, for an example
 * we get that "iname rd, rs, rt".tokenize() yields ["rd", "rs", "rt"]
 *
 * @param includeIname exclude the "iname" in the return
 */
fun String.tokenize(includeIname: Boolean = true): Array<String> = {
  var a = this.trim().replace(",", " ").replace(Regex("\\s+"), " ").split(" ").toTypedArray()
  if (!includeIname) {
    // Remove the iname
    a = Arrays.copyOfRange(a, 1, a.size)
  }
  a
}.invoke()

fun String.iname(): String = this.tokenize()[0]

/**
 * Will compare two given Strings that will be compared formatting. Since the
 * mnemonic do not need to be identical to be equal they need a more excessive
 * comparision.
 *
 * For example the Register '$a0' and '$4' is equal. Also can formatting of
 * numbers be done in different bases such as hexadecimal or decimal.
 */
fun mnemonicEquals(s1: String, s2: String): Boolean {
  val tokens1 = s1.tokenize(includeIname = false)
  val tokens2 = s2.tokenize(includeIname = false)
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
 * Check if String contains parentheses when excepted.
 * For example can not Format.J or Format.J contain parentheses but
 * Format.I can, however Format.I is not exclusively with parentheses.
 */
fun throwIfInvalidParentheses(standardizedMnemonic: String, format : Format) {
  if (format == Format.R || format == Format.J) {
    if (standardizedMnemonic.containsParentheses()) {
      throw IllegalCharactersInMnemonicException(
        standardizedMnemonic, "<parentheses>")
    }
  }
}

/**
 * Check if given String contains excepted number of commas.
 * Example:
 * add $t1, $t2, $t3    (2)
 * jr $t1               (0)
 * break                (0)
 */
fun throwIfIncorrectNumberOfCommas(expectedNumberOfCommas: Int, standardizedMnemonic: String) {
  val actualNumberOfCommas = standardizedMnemonic.countCommas()

  if (actualNumberOfCommas != expectedNumberOfCommas) {
    val err = "\"%s\": Wrong number of commas: Expected: %d. Got: %d".format(
      standardizedMnemonic, expectedNumberOfCommas, actualNumberOfCommas)
    throw IllegalArgumentException(err)
  }
}

/**
 * Replace all white-space characters (\\s+) with a single and will correct
 * commas so always followed with a white-space. Will remove any leading
 * or trailing spaces (trim) to normalise String.
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

/**
 * Check if given String contains correct number of arguments. Arguments is
 * how many parameters an instruction has been given.
 * Example:
 * add $t1, $t2, $t3   (3)
 * jr $t1             (1)
 * break              (0)
 */
fun throwIfIncorrectNumberOfArgs(expectedArgc: Int, standardizedMnemonic : String) {
  // -1 for the instruction name
  val withoutCommas = standardizedMnemonic.replace(",", "")
  val actualArgc = withoutCommas.split(" ").size - 1

  if (expectedArgc == actualArgc) { return }

  val err = "\"%s\": Expected %d arguments. Got: %d".format(
    standardizedMnemonic, expectedArgc, actualArgc)
  throw IllegalArgumentException("Wrong number of arguments: " + err)
}





