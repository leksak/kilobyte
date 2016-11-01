package common.instruction.mnemonic

import common.instruction.rt
import common.instruction.rd
import common.instruction.rs
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

private fun standardizeMnemonic(mnemonic: String): String {
  // Begin by replacing all commas with a space,
  // thereby transforming:
  //
  // add $t1,$t2, $t3 (intentional space before $t3)
  //
  // so we get
  //
  // add $t1, $t2,   $t3 (triple space before $t3)
  //
  // Then, replace all white-space characters (\\s+) with a single
  // space and remove any leading or trailing spaces (trim).
  //
  // This would standardize both "add $t1, $t2, $t3" and
  // "    add $t1,$t2,  $t3  " to the same string, namely
  // "add $t1, $t2, $t3".
  //
  // This sequence of operations also standardizes
  // "jr $t1" to "jr $t1" (identity transformation).
  return mnemonic.replace(",", ", ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

// For use with add, etc. but not sw, etc.
fun throwExceptionIfContainsParentheses(standardizedMnemonic: String) {
  if (standardizedMnemonic.matches(Regex(".*[()].*"))) {
    throw IllegalCharactersInMnemonicException(
          standardizedMnemonic, "<parentheses>");
  }
}

fun throwExceptionIfContainsIllegalCharacters(standardizedMnemonic: String) {
  // Throw an exception if the passed string contains a new line character
  val newline = System.getProperty("line.separator");
  if (standardizedMnemonic.contains(newline)) {
    throw
    IllegalCharactersInMnemonicException(standardizedMnemonic, "<newline>")
  }

  // Check for other illegal characters:
  // goo.gl/Q8EiLb
  val regex = "[A-Za-z, ()0-9]"
  val p = Pattern.compile(regex)
  val matcher = p.matcher(standardizedMnemonic)

  // Get rid of any pre-existing plus signs ?
  val mask = matcher.replaceAll("+").replace("[^+]", "-")

  if (mask.contains("-")) {
    // At least one illegal character was detected,
    val illegalCharacters = java.util.StringJoiner("', '", "['", "']")
    for (i in 0..standardizedMnemonic.length) {
      if (mask[i] == '-') {
        illegalCharacters.add(standardizedMnemonic[i].toString())
      }
    }
    throw IllegalCharactersInMnemonicException(
          standardizedMnemonic, illegalCharacters)
  }
}

fun throwIfIncorrectNumberOfCommas(expectedNumberOfCommas: Int, standardizedMnemonic: String) {
  val actualNumberOfCommas = StringUtils.countMatches(standardizedMnemonic, ",");

  if (actualNumberOfCommas != expectedNumberOfCommas) {
    val err = "Wrong number of commas: %s. Expected: %d. Got: %d".format(
          standardizedMnemonic, expectedNumberOfCommas, actualNumberOfCommas)
    throw IllegalArgumentException(err);
  }
}

fun throwIfIncorrectNumberOfArgs(expectedArgc: Int, standardizedMnemonic : String) {
  // -1 for the instruction name
  val withoutCommas = standardizedMnemonic.replace(",", "")
  val actualArgc = withoutCommas.split(" ").size - 1

  if (expectedArgc == actualArgc) { return }

  val err = "\"%s\": Expected %d arguments. Got: %d".format(
        standardizedMnemonic, expectedArgc, actualArgc)
  throw IllegalArgumentException("Wrong number of arguments: " + err)
}

/**
 * We need to be able to create mnemonics out of strings
 */
fun INAME_RD_RS_RTfromString(mnemonic: String): MnemonicRepresentation {
  val argc = 3

  // There should be as many commas as there are (expected) arguments - 1,
  // For an example "add $t1, $t2, $t3" has 3 arguments ($t1, $t2, $t3)
  // so there should be two commas.
  throwIfIncorrectNumberOfCommas(2, mnemonic)

  val standardizedMnemonic = standardizeMnemonic(mnemonic)
  throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

  // This pattern shouldn't contain any parens
  throwExceptionIfContainsParentheses(standardizedMnemonic)
  throwIfIncorrectNumberOfArgs(argc, standardizedMnemonic)

  return MnemonicRepresentation(standardizedMnemonic)
}

/**
 * And perform the inverse operation!
 */
fun INAME_RD_RS_RTfromMachineCode(machineCode: Int): MnemonicRepresentation {
  // TODO: Don't forget to apply all the condition masks
  val iname = common.instruction.Instruction.InstructionSet.getIname(machineCode)
  val rd = common.hardware.Register.fromInt(machineCode.rd()).toString()
  val rs = common.hardware.Register.fromInt(machineCode.rs()).toString()
  val rt = common.hardware.Register.fromInt(machineCode.rt()).toString()
  return MnemonicRepresentation(iname, rd, rs, rt)
}

fun nopFromString(mnemonic: String): MnemonicRepresentation {
  throwExceptionIfContainsIllegalCharacters(mnemonic)

  // This pattern shouldn't contain any parens
  throwExceptionIfContainsParentheses(mnemonic)
  throwIfIncorrectNumberOfArgs(0, mnemonic)

  return MnemonicRepresentation("nop")
}

fun nopFromMachineCode(machineCode : Int): MnemonicRepresentation {
  if (machineCode != 0) {
    val err = "Attempted to initialize \"nop\" from a non-zero value: $machineCode"
    throw IllegalArgumentException(err)
  }
  return MnemonicRepresentation("nop")
}

class MnemonicPattern(
      val fromStringToMnemonic: (String) -> MnemonicRepresentation,
      val fromMachineCodeToMnemonic: (Int) -> MnemonicRepresentation
) {
  companion object Factory {
    @JvmField val NOP_PATTERN = MnemonicPattern(
          ::nopFromString,
          ::nopFromMachineCode
    )
    @JvmField val INAME_RD_RS_RT = MnemonicPattern(
          ::INAME_RD_RS_RTfromString,
          ::INAME_RD_RS_RTfromMachineCode)
  }
}

