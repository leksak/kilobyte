package common.instruction.mnemonic

import org.apache.commons.lang3.StringUtils
import java.util.*

fun String.containsParentheses() = this.matches(Regex(".*[()].*"))
fun String.countCommas(): Int = StringUtils.countMatches(this, ",")
fun String.containsNewline(): () -> Boolean = {
  this.contains(System.getProperty("line.separator"))
}
fun String.tokenize(): Array<String> = {
  this.trim().replace(Regex("\\s+"), " ").split(" ").toTypedArray()
}.invoke()

fun String.iname(): String = this.tokenize()[0]



