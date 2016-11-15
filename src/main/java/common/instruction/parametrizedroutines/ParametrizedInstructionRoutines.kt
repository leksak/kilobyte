package common.instruction.parametrizedroutines

import com.google.common.base.Preconditions.checkArgument
import common.hardware.Register
import common.instruction.*
import io.atlassian.fugue.Either

/**
 * Using the mnemonic we can discern the number of commas, arguments
 * etc. Using the format we can discern the default values for all the
 * fields.
 */
fun from(format: Format, mnemonic: String): ParametrizedInstructionRoutine {
  // No need to standardize, this should only be called from the
  // Instruction constructors.
  val m = standardizeMnemonic(mnemonic)
  val expectedNumberOfCommas = m.countCommas()
  val expectedNumberOfArguments =
        m.replace(",", "").split(" ").size - 1

  fun mnemonicCheck(mnemonicRepresentation : String) {
    throwIfIncorrectNumberOfCommas(expectedNumberOfCommas, mnemonicRepresentation)

    val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
    throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

    if (format == Format.R || format == Format.J) {
      // This pattern shouldn't contain any parens
      throwExceptionIfContainsParentheses(standardizedMnemonic)
    }

    throwIfIncorrectNumberOfArgs(expectedNumberOfArguments, standardizedMnemonic)
  }

  return object : ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction,
                        mnemonicRepresentation: String): Instruction
    {
      mnemonicCheck(mnemonicRepresentation)
      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      checkArgument(prototype.iname == standardizedMnemonic.iname())

      val tokens: Array<String> = standardizedMnemonic.tokenize()

      val opcode = prototype.opcode

      when (format) {
        Format.R -> {
          val rd = Register.fromString(tokens[1]).asInt()
          val rs = Register.fromString(tokens[2]).asInt()
          val rt = Register.fromString(tokens[3]).asInt()
          val shamt = 0
          val funct = prototype.funct!!

          val numericRepresentation = Format.fieldsToMachineCode(opcode, rs, rt, rd, shamt, funct)
          return prototype(standardizedMnemonic, numericRepresentation)
        }
        else -> {
          throw IllegalStateException("Attempted to instantiate " +
                "a instruction from an unknown format. Format: $format")
        }
      }
    }

    override fun invoke(prototype: Instruction, machineCode: Long):
          Either<Instruction, PartiallyValidInstruction>
    {
      val iname = prototype.iname

      when (format) {
        Format.R -> {
          val rd = Register.fromInt(machineCode.rd()).toString()
          val rs = Register.fromInt(machineCode.rs()).toString()
          val rt = Register.fromInt(machineCode.rt()).toString()

          val inst = prototype("$iname $rd, $rs, $rt", machineCode)
          if (machineCode.shamt() != 0) {
            val err = "Expected shamt to be zero. Got ${machineCode.shamt()}"
            return Either.right(PartiallyValidInstruction(inst, err))
          }

          // Create a new copy using these values
          return Either.left(inst)
        }
        else -> {
          throw IllegalStateException("Attempted to instantiate " +
                "a instruction from an unknown format. Format: $format")
        }
      }
    }
  }
}

@JvmField val INAME_RD_RS_RT = from(Format.R, "iname rd, rs, rt")
