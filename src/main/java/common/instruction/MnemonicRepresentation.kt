package common.instruction;

import common.hardware.Register
import org.apache.commons.lang3.StringUtils

import java.util.StringJoiner
import java.util.regex.Pattern

private fun standardizeMnemonic(mnemonic: String): String {
  // Begin by replacing all commas with a space,
  // thereby transforming:
  //
  // add $t1,$t2, $t3 (intentional space before $t3)
  //
  // so we get
  //
  // add $t1 $t2  $t3 (double space before $t3)
  //
  // Then, replace all white-space characters (\\s+) with a single
  // space and remove any leading or trailing spaces (trim).
  //
  // This would standardize both "add $t1, $t2, $t3" and
  // "    add $t1,$t2,  $t3  " to the same string, namely
  // "add $t1 $t2 $t3". This sequence of operations also standardizes
  // "jr $t1" to "jr $t1" (identity transformation).
  return mnemonic.replace(",", " ")
        .replace("\\s+", " ")
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
  val p = Pattern.compile(regex);
  val matcher = p.matcher(standardizedMnemonic)

  // Get rid of any pre-existing plus signs ?
  val mask = matcher.replaceAll("+").replace("[^+]", "-");

  if (mask.contains("-")) {
    // At least one illegal character was detected,
    val illegalCharacters = StringJoiner("', '", "['", "']")
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
  // - 1 for the instruction name
  val actualArgc = standardizedMnemonic.split(" ").size - 1

  if (expectedArgc == actualArgc) { return }

  val err = "\"%s\": Expected %d arguments. Got: %d".format(
        standardizedMnemonic, expectedArgc, actualArgc)
  throw IllegalArgumentException("Wrong number of arguments: " + err)
}

/**
 * We need to be able to create mnemonics out of strings
 */
fun INAME_RD_RS_RT(mnemonic: String): MnemonicRepresentation {
  val argc = 3

  // There should be as many commas as there are arguments - 1,
  // For an example "add $t1, $t2, $t3" has 3 arguments ($t1, $t2, $t3)
  // so there should be two commas.
  throwIfIncorrectNumberOfCommas(argc - 1, mnemonic)

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
fun INAME_RD_RS_RT(machineCode: Int): MnemonicRepresentation {
  // TODO: Don't forget to apply all the condition masks
  val iname = Instruction.getIname(machineCode)
  val rd = Register.fromInt(machineCode.rd()).toString()
  val rs = Register.fromInt(machineCode.rs()).toString()
  val rt = Register.fromInt(machineCode.rt()).toString()
  return MnemonicRepresentation(iname, rd, rs, rt)
}

fun NOP_PATTERN(mnemonic: String): MnemonicRepresentation {
  val standardizedMnemonic = standardizeMnemonic(mnemonic)
  throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

  // This pattern shouldn't contain any parens
  throwExceptionIfContainsParentheses(standardizedMnemonic)
  throwIfIncorrectNumberOfArgs(0, standardizedMnemonic)

  return MnemonicRepresentation("nop")
}


class MnemonicRepresentation {
  val standardizedMnemonic: String
  val iname: String

  constructor(mnemonic: String) {
    iname = mnemonic.split(' ')[0]
    Instruction.getPattern(iname).invoke(mnemonic)
    standardizedMnemonic = standardizeMnemonic(mnemonic)
  }


  internal constructor(iname: String,
                       vararg args: String) {
    // TODO: Will also probably have to capture the numeric rep here
    val sj = StringJoiner(", ")
    args.map { sj.add(it) }
    this.standardizedMnemonic = "%s %s".format(iname, sj.toString())
    this.iname = iname
    println(iname)
    println(args)
    Instruction.getPattern(iname).invoke(standardizedMnemonic)
  }
}
