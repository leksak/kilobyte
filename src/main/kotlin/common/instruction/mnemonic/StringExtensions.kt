package common.instruction.mnemonic

import org.apache.commons.lang3.StringUtils

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

fun String.remove(substring: String) = this.replace(substring, "")
fun String.removeCommas() = this.remove(",")

/**
 * Will tokenize a String using commas as the default delimiter.
 * All leading and trailing whitespace is trimmed. All whitespace
 * within the String are ignored in the tokenization process. For
 * an example we get that "iname rd, rs, rt".tokenize() yields
 * ["iname", "rd", "rs", "rt"]
 *
 * @param delimiter the character that delimits tokens
 */
fun String.tokenize(delimiter: String = ","): Array<String> = {
  this.trim().replace(delimiter, " ").replace(Regex("\\s+"), " ").split(" ").toTypedArray()
}.invoke()





