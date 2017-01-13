package common.hardware


import com.google.common.base.Preconditions.checkArgument
import common.instruction.Instruction
import common.machinecode.*
import kotlin.reflect.KFunction1

class RegisterFile {
  /**
   * Each RegisterFile has its own set of registers avoiding any problems
   * that may arise by having shared state through the use of static constructs
   * such as an object.
   *
   * This "feature" allows us to take intermediate snapshots of the simulator
   * which affords us the possibility of undo-ing instructions.
   */
  val registers = arrayOf(
        // Registers 0 through 3
        Register(0, "\$zero", "Constant 0"),
        Register(1, "\$at", "Reserved for assembler"),
        Register(2, "\$v0", "Expression evaluation and results of a function"),
        Register(3, "\$v1", "Expression evaluation and results of a function"),

        // Registers 4 through 7
        Register(4, "\$a0", "Argument 1"),
        Register(5, "\$a1", "Argument 2"),
        Register(6, "\$a2", "Argument 3"),
        Register(7, "\$a3", "Argument 4"),

        // Registers 8 through 11
        Register(8, "\$t0", "Temporary  = Register(not preserved across call)"),
        Register(9, "\$t1", "Temporary  = Register(not preserved across call)"),
        Register(10, "\$t2", "Temporary  = Register(not preserved across call)"),
        Register(11, "\$t3", "Temporary  = Register(not preserved across call)"),

        // Registers 12 through 15
        Register(12, "\$t4", "Temporary  = Register(not preserved across call)"),
        Register(13, "\$t5", "Temporary  = Register(not preserved across call)"),
        Register(14, "\$t6", "Temporary  = Register(not preserved across call)"),
        Register(15, "\$t7", "Temporary  = Register(not preserved across call)"),

        // Registers 16 through 19
        Register(16, "\$s0", "Saved temporary  = Register(preserved across call)"),
        Register(17, "\$s1", "Saved temporary  = Register(preserved across call)"),
        Register(18, "\$s2", "Saved temporary  = Register(preserved across call)"),
        Register(19, "\$s3", "Saved temporary  = Register(preserved across call)"),

        // Registers 20 through 23
        Register(20, "\$s4", "Saved temporary  = Register(preserved across call)"),
        Register(21, "\$s5", "Saved temporary  = Register(preserved across call)"),
        Register(22, "\$s6", "Saved temporary  = Register(preserved across call)"),
        Register(23, "\$s7", "Saved temporary  = Register(preserved across call)"),

        // Registers 24 through 27
        Register(24, "\$t8", "Temporary  = Register(not preserved across call)"),
        Register(25, "\$t9", "Temporary  = Register(not preserved across call)"),

        Register(26, "\$k0", "Reserved for OS kernel"),
        Register(27, "\$k1", "Reserved for OS kernel"),

        // Registers 28 through 31  = Register(32 total)
        Register(28, "\$gp", "Pointer to global area"),
        Register(29, "\$sp", "Stack pointer"),
        Register(30, "\$fp", "Frame pointer"),

        Register(31, "\$ra", "Return offset  = Register(used by function call)")
  )
  // Creates a lookup table for getting the associate index given a specific name,
  // i.e. nameToIndexLookup["$zero"] == "$zero"
  val nameToRegisterLookup = registers.associateBy({ it.name })

  operator fun get(machineCode: Int): Register {
    checkArgument(machineCode in 0..31, "Expected $machineCode to be in range [0, 32)")
    return registers[machineCode]
  }

  fun get(f :Field , i: Instruction) : Register = get(f.getFunc(i.numericRepresentation))

  fun writeToRegister(f : Field, i : Instruction, value : Int) {
    get(f, i).value = value
  }

  fun reset() {
    for (r in registers) {
      r.value = 0
    }
  }

  operator fun get(mnemonic: String): Register {
    checkArgument(mnemonic.startsWith("$"), "Registers has to start with a \"$\". Got $mnemonic")
    val sansDollarSign = mnemonic.replace("$", "")

    if (sansDollarSign.matches(Regex("\\d+"))) {
      // A non-symbolic name was passed, such as "$8" as opposed
      // to the symbolic "$t0".
      return registers[sansDollarSign.toInt()]
    } else {
      // A symbolic name was passed.
      return nameToRegisterLookup[mnemonic]!!
    }
  }

  companion object {
    val rf: RegisterFile = RegisterFile()

    /**
     * Returns the symbolic version of the supplied register meaning that
     * get($0) yields $zero and get($zero) yields $zero.
     */
    @JvmStatic fun getMnemonic(mnemonic: String): String = rf[mnemonic].toString()

    /**
     * Returns the string representation of the register at the given index, i.e.
     * get(0) is equal to $zero.
     */
    @JvmStatic fun getMnemonic(index: Int): String = rf[index].toString()

    /**
     * Interprets the given String and returns the index of that register,
     * i.e. indexOf($t0) == 8
     */
    @JvmStatic fun indexOf(mnemonic: String): Int = rf[mnemonic].index
  }

}

enum class Field(val getFunc: KFunction1<MachineCode, Int>) {
  RD(MachineCode::rd),
  RT(MachineCode::rt),
  RS(MachineCode::rs),
  TARGET(MachineCode::target)
}
