package common.extensions


import common.hardware.RegisterFile
import decompiler.MachineCodeDecoder
import org.apache.commons.lang3.StringUtils

/**
 * Returns true of the given String contains any parentheses, i.e.
 *
 * "foo(".containsParentheses() is true
 * "bar)".containsParentheses() is true
 * "b()az".containsParentheses() is true
 * "alice".containsParentheses() is false
 */
fun String.containsParentheses() = this.matches(Regex(".*[()].*"))

/**
 * Returns the number of commas of given String, i.e.
 *
 * "foo,,bar".countCommas = 2
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
 * an example we get that {@code "iname rd, rs, rt".tokenize()} yields
 * {@code ["iname", "rd", "rs", "rt"]} and that {@code "10($t0)".tokenize("(")}
 * yields {@code ["10", "$t0"]}
 *
 * @param delimiter the character that delimits tokens
 */
fun String.tokenize(delimiter: String = ","): Array<String> = {
  this.trim().replace(delimiter, " ").replace(Regex("\\s+"), " ").split(" ").toTypedArray()
}.invoke()

/**
 * For a String on the form OFFSET($REG) this function yields OFFSET, meaning that for
 * "10($t0)".getOffset() is equal to "10"
 */
fun String.getOffset(): Int = MachineCodeDecoder.decode(this.remove(")").tokenize("(")[0]).toInt()

/**
 * For a String on the form OFFSET($REG) this function yields OFFSET, meaning that for
 * "10($t0)".getRegister() is equal to "$t0"
 */
fun String.getRegister(): String = RegisterFile[this.remove(")").tokenize("(")[1]]
