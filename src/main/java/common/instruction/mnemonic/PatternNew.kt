package common.instruction.mnemonic

import common.hardware.Register
import common.instruction.Instruction
import common.instruction.rd
import common.instruction.rs
import common.instruction.rt

/*
* For an example, R-format instructions expressed on the form
*
* iname rd, rs, rt (such as add)
*
* require that the shamt field be zero.
*/
class INAME_RD_RS_RT {
  // TODO: Return partially legal instruction when shamt is not 0
  fun invoke(prototype: Instruction, machineCode: Int): Instruction {
    val iname = prototype.iname
    val rd = Register.fromInt(machineCode.rd()).toString()
    val rs = Register.fromInt(machineCode.rs()).toString()
    val rt = Register.fromInt(machineCode.rt()).toString()
    val mnemonic = "$iname $rd, $rs, $rt"

    // Todo check that machinecode shamt == 0, otherwise "exception"

    // Create a new copy using these values
    return prototype(mnemonic, machineCode)
  }

  fun invoke(prototype: Instruction, mnemonic: String): Instruction {
    val numberOfCommas = mnemonic.countCommas()
    if (numberOfCommas != 2) {
      val err = "Wrong number of commas in \"%s\". Expected: 2. Got: %d".format(
            mnemonic, numberOfCommas)
      throw IllegalArgumentException(err)
    }

    val standardizedMnemonic = standardizeMnemonic(mnemonic)
    throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

    // This pattern shouldn't contain any parens
    throwExceptionIfContainsParentheses(standardizedMnemonic)
    throwIfIncorrectNumberOfArgs(2, standardizedMnemonic)

    val tokens: Array<String> = standardizedMnemonic.tokenize()
    val rd = tokens[1]
    val rs = tokens[2]
    val rt = tokens[3]++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  }
}
