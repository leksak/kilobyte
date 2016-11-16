package common.instruction.parametrizedroutines

import com.google.common.base.Preconditions.checkArgument
import common.hardware.Register
import common.instruction.*
import common.instruction.exceptions.IllegalCharactersInMnemonicException
import io.atlassian.fugue.Either
import java.util.*
import java.util.regex.Pattern


interface ParametrizedInstructionRoutine {
  fun invoke(prototype: Instruction, machineCode: Long):
        Either<Instruction, PartiallyValidInstruction>
  fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction




  
  object NOP: ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction, machineCode: Long): Either<Instruction, PartiallyValidInstruction> {
      if (machineCode.equals(0)) {
        return Either.left(Instruction.NOP)
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
   * iname (such as syscall)
   *
   * require that the shamt field be zero.
   */
  object INAME: ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction, machineCode: Long):
          Either<Instruction, PartiallyValidInstruction> {
      val iname = prototype.iname
      val mnemonic = iname

      val inst = prototype(mnemonic, machineCode)
      if (machineCode.shamt() != 0) {
        val err = "Expected shamt to be zero. Got ${machineCode.shamt()}"
        return Either.right(PartiallyValidInstruction(inst, err))
      }

      // Create a new copy using these values
      return Either.left(inst)
    }

    override fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction {
      checkArgument(prototype.iname == mnemonicRepresentation.iname())
      throwIfIncorrectNumberOfCommas(0, mnemonicRepresentation)

      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

      // This pattern shouldn't contain any parens
      throwExceptionIfContainsParentheses(standardizedMnemonic)
      throwIfIncorrectNumberOfArgs(0, standardizedMnemonic)

      val opcode = prototype.opcode
      val rd = 0
      val rs = 0
      val rt = 0
      val shamt = 0
      val funct = prototype.funct!!

      val numericRepresentation = Format.fieldsToMachineCode(opcode, rs, rt, rd, shamt, funct)
      return prototype(standardizedMnemonic, numericRepresentation)
    }
  }


  /* R-format instructions expressed on the form
  *
  * iname rs (such as jr)
  *
  * require that the shamt field be zero.
  */
  object INAME_RS: ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction, machineCode: Long):
            Either<Instruction, PartiallyValidInstruction> {
      val iname = prototype.iname
      val rs = Register.fromInt(machineCode.rs()).toString()
      val mnemonic = "$iname $rs"

      val inst = prototype(mnemonic, machineCode)
      if (machineCode.shamt() != 0) {
        val err = "Expected shamt to be zero. Got ${machineCode.shamt()}"
        return Either.right(PartiallyValidInstruction(inst, err))
      }

      // Create a new copy using these values
      return Either.left(inst)
    }

    override fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction {
      checkArgument(prototype.iname == mnemonicRepresentation.iname())
      throwIfIncorrectNumberOfCommas(0, mnemonicRepresentation)

      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

      // This pattern shouldn't contain any parens
      throwExceptionIfContainsParentheses(standardizedMnemonic)
      throwIfIncorrectNumberOfArgs(1, standardizedMnemonic)

      val tokens: Array<String> = standardizedMnemonic.tokenize()
      val opcode = prototype.opcode
      val rd = 0
      val rs = Register.fromString(tokens[1]).asInt()
      val rt = 0
      val shamt = 0
      val funct = prototype.funct!!

      val numericRepresentation = Format.fieldsToMachineCode(opcode, rs, rt, rd, shamt, funct)
      return prototype(standardizedMnemonic, numericRepresentation)
    }
  }

  /**
   * All I-format instructions are decomposed into fields of the
   * same length.
   *
   * An I-type instruction is determined uniquely by its opcode field.
   *
   * The opcode is the leftmost 6-bits of the instruction when
   * represented as a 32-bit number.
   *
   * The bit-fields then represent the
   * following units of effect
   *
   * | 6 bits  | 5 bits | 5 bits | 16 bits |
   * |:-------:|:------:|:------:|:-------:|
   * | op      | rs     | rt     | offset  |
   *
   * This container object which contains a tuple of functions is
   * to be used for instructions on the form
   *
   * iname rt, address
   *
   * For an example,
   *
   * lw $t0, 24($s2)
   *
   * which is represented numerically as (cite 5DV118 20131110 t:2B sl:18)
   *
   * | op      | rs     | rt     | offset  |
   * |:-------:|:------:|:------:|:-------:|
   * | 0x23    | 18     | 8      | 24_{10} |
   *
   * Note: the semantics of the instruction is
   * Load word : Set $t0 to contents of effective memory word address",
   */
  /*object INAME_RT_RS_ADDR : ParametrizedInstructionRoutine {
    fun invoke(prototype: Instruction, machineCode: Long):
          Either<Instruction, PartiallyValidInstruction> {

    }

    fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction {

    }
  }*/
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