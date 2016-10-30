package common.instruction

class InstructionSet {
  val shamt_is_zero: Condition = Condition(
        {it -> if (it.shamt() == 0) {
          ConditionResult.Success()
        } else {
          ConditionResult.Failure("Shamt has to be zero. Got: " + it)
        }}
  )

  val instructionSet: Array<InstructionPrototype> =
    arrayOf(
          InstructionPrototype(
                iname = "add",
                opcode = 0,
                mnemonicExample = "add \$t1, \$t2, \$t3",
                numericExample = 0x014b4820,
                description = "Addition with overflow,. Put the" +
                      " sum of registers rs and rt into register" +
                      " rd. Is only valid if shamt is 0.",
                format = Format.R,
                pattern = Pattern.INAME_RD_RS_RT,
                funct = 0x20,
                conditions = shamt_is_zero)
    )

  /**
   * Prints the names of all the instructions contained in this set.
   * Useful for technical documentation.
   *
   * @param onlyExecutables Set to true to only print the functions
   *                        which are executable. (not yet supported)
   */
  fun printInstructionSet(onlyExecutables: Boolean = false) {
    for (prototype in instructionSet) {
      println(prototype.iname)
    }
  }
}
