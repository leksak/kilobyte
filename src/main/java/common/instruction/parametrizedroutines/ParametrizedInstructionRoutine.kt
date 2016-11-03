package common.instruction.parametrizedroutines

import io.atlassian.fugue.Either

import com.google.common.base.Preconditions.checkArgument
import common.hardware.Register
import common.instruction.*
import common.instruction.exceptions.IllegalCharactersInMnemonicException
import java.util.*
import java.util.regex.Pattern


interface ParametrizedInstructionRoutine {
  fun invoke(prototype: Instruction, machineCode: Long):
        Either<Instruction, PartiallyValidInstruction>
  fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction

  object NOP: ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction, machineCode: Long): Either<Instruction, PartiallyValidInstruction> {
      if (machineCode.equals(0)) {
        return Either.left(Instruction.NOP);
      }
      throw IllegalArgumentException("Cannot instantiate \"nop\" from: $machineCode")
    }

    override fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction {
      checkArgument(prototype.iname == mnemonicRepresentation.iname())
      throwIfIncorrectNumberOfCommas(0, mnemonicRepresentation)

      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

      // This pattern shouldn't contain any parens
      throwExceptionIfContainsParentheses(standardizedMnemonic)

      // Should have zero arguments
      throwIfIncorrectNumberOfArgs(0, standardizedMnemonic)
      return Instruction.NOP
    }
  }

  /*
   * For an example, R-format instructions expressed on the form
   *
   * iname rd, rs, rt (such as add)
   *
   * require that the shamt field be zero.
   */
  object INAME_RD_RS_RT: ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction, machineCode: Long):
          Either<Instruction, PartiallyValidInstruction> {
      val iname = prototype.iname
      val rd = Register.fromInt(machineCode.rd()).toString()
      val rs = Register.fromInt(machineCode.rs()).toString()
      val rt = Register.fromInt(machineCode.rt()).toString()
      val mnemonic = "$iname $rd, $rs, $rt"

      val inst = prototype(mnemonic, machineCode)
      if (machineCode.shamt() != 0) {
        //val err = "Expected shamt to be zero. Got ${machineCode.shamt()}"
        return Either.right(PartiallyValidInstruction(inst, "hej"))
      }

      // Create a new copy using these values
      return Either.left(inst)
    }

    override fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction {
      checkArgument(prototype.iname == mnemonicRepresentation.iname())
      throwIfIncorrectNumberOfCommas(2, mnemonicRepresentation)

      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

      // This pattern shouldn't contain any parens
      throwExceptionIfContainsParentheses(standardizedMnemonic)
      throwIfIncorrectNumberOfArgs(3, standardizedMnemonic)

      val tokens: Array<String> = standardizedMnemonic.tokenize()
      val opcode = prototype.opcode
      val rd = Register.fromString(tokens[1]).asInt()
      val rs = Register.fromString(tokens[2]).asInt()
      val rt = Register.fromString(tokens[3]).asInt()
      val shamt = 0
      val funct = prototype.funct!!

      val numericRepresentation = Format.fieldsToMachineCode(opcode, rs, rt, rd, shamt, funct)
      return prototype(standardizedMnemonic, numericRepresentation)
    }
  }
}

// For use with add, etc. but not sw, etc.
fun throwExceptionIfContainsParentheses(standardizedMnemonic: String) {
  if (standardizedMnemonic.containsParentheses()) {
    throw IllegalCharactersInMnemonicException(
          standardizedMnemonic, "<parentheses>")
  }
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
  val regex = "[A-Za-z, ()0-9\$]"
  val p = Pattern.compile(regex)
  val matcher = p.matcher(standardizedMnemonic)
  // TODO:Get rid of any pre-existing plus signs?
  //
  // Given the input
  //
  // add $t1, $t2, $t3!#$sp
  //
  // then the below statement will yield a mask with "+" signs on characters
  // that match the regex and "-" signs on characters that do not
  // match, i.e. illegal characters. So, for the above example the
  // match will be
  //
  // mask == +++++++++++++++++--+++
  val mask = matcher.replaceAll("+").replace(Regex("[^+]"), "-")
  if (mask.contains("-")) {
    // At least one illegal character was detected,
    val illegalCharacters = StringJoiner("', '", "['", "']")
    for (i in 0..(standardizedMnemonic.length - 1)) {
      if (mask[i] == '-') {
        illegalCharacters.add(standardizedMnemonic[i].toString())
      }
    }
    throw IllegalCharactersInMnemonicException(
          standardizedMnemonic, illegalCharacters)
  }
}

fun throwIfIncorrectNumberOfCommas(expectedNumberOfCommas: Int, standardizedMnemonic: String) {
  val actualNumberOfCommas = standardizedMnemonic.countCommas()

  if (actualNumberOfCommas != expectedNumberOfCommas) {
    val err = "\"%s\": Wrong number of commas: Expected: %d. Got: %d".format(
          standardizedMnemonic, expectedNumberOfCommas, actualNumberOfCommas)
    throw IllegalArgumentException(err)
  }
}

fun standardizeMnemonic(mnemonic: String): String {
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
  // This would normalise both "add $t1, $t2, $t3" and
  // "    add $t1,$t2,  $t3  " to the same string, namely
  // "add $t1, $t2, $t3".
  //
  // This sequence of operations also normalises
  // "jr $t1" to "jr $t1" (identity transformation).
  return mnemonic.replace(",", ", ")
        .replace(Regex("\\s+"), " ")
        .trim()
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