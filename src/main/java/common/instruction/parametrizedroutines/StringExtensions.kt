package common.instruction.parametrizedroutines

import common.hardware.Register
import org.apache.commons.lang3.StringUtils
import java.util.*

fun String.containsParentheses() = this.matches(Regex(".*[()].*"))
fun String.countCommas(): Int = StringUtils.countMatches(this, ",")
fun String.containsNewlineCharacter(): Boolean = {
  this.contains(System.getProperty("line.separator"))
}.invoke()

fun String.tokenize(includeIname: Boolean = true): Array<String> = {
  var a = this.trim().replace(",", " ").replace(Regex("\\s+"), " ").split(" ").toTypedArray()
  if (!includeIname) {
    // Remove the name
    a = Arrays.copyOfRange(a, 1, a.size)
  }
  a
}.invoke()

fun String.iname(): String = this.tokenize()[0]

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
        // Neither string is a register so compare them for simple equality
        return false
      }
    }
  }
  return true
}




