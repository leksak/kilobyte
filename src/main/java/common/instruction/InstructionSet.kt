package common.instruction

import java.util.*

// Singleton data container:
// https://kotlinlang.org/docs/reference/idioms.html#creating-a-singleton
object InstructionSet {
  val shamt_is_zero: Condition = Condition(
        {it -> if (it.shamt() == 0) {
          ConditionResult.Success()
        } else {
          ConditionResult.Failure("Shamt has to be zero. Got: " + it)
        }}
  )

  val prototypeSet: Array<InstructionPrototype> =
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
  // Lookup table
  // You can take the name of an Instruction and get the associated
  // Prototype, from which you can spawn your instruction provided
  // you have a symbolic representation of it.
  val inameToPrototype: HashMap<String, InstructionPrototype> = HashMap()
  val opcodeEquals0x00IdentifiedByFunct: Array<InstructionPrototype> = emptyArray()
  val opcodeEquals0x01IdentifiedByRt: Array<InstructionPrototype> = emptyArray()
  val opcodeEquals0x1cIdentifiedByFunct: Array<InstructionPrototype> = emptyArray()
  val identifiedByTheirOpcodeAlone: Array<InstructionPrototype> = emptyArray()

  init {
    for (prototype in prototypeSet) {
      val iname = prototype.iname
      inameToPrototype.put(iname, prototype)

      // Nop is all zeroes and clashes with sll which has opcode=0x00
      // and funct=0x00. We treat nop as a special case.
      if (iname == "nop") {
        continue
      }

      if (prototype.opcode == 0) {
        // Throws an IllegalStateException if the funct field has not
        // been set, that seems appropriate as that would be a programmer
        // error by _us_. If it is set (not null) we get its value.
        //
        // Any following lines of code "knows" that funct is guaranteed
        // to be not-null.
        val funct = checkNotNull(prototype.funct)
        opcodeEquals0x00IdentifiedByFunct[funct] = prototype
      } else if (prototype.opcode == 0x1c) {
        val funct = checkNotNull(prototype.funct)
        opcodeEquals0x1cIdentifiedByFunct[funct] = prototype
      } else if (prototype.opcode == 0x01) {
        val rt = checkNotNull(prototype.rt)
        opcodeEquals0x01IdentifiedByRt[rt] = prototype
      } else {
        identifiedByTheirOpcodeAlone[prototype.opcode] = prototype
      }
    }
  }

  @JvmStatic fun get(iname: String): Optional<InstructionPrototype> {
    return Optional.ofNullable(inameToPrototype[iname])
  }

  @JvmStatic fun get(machineCode: Int): Optional<InstructionPrototype> {
    val opcode = machineCode.opcode()

    // Check if the entire number is 0s, then we have a nop instruction
    // Once again, nop and sll clashes on the (opcode, funct) tuple so
    // we have to treat one of them as a special-case. Nop seemed easiest
    // to handle as a special case.
    if (machineCode == 0x00) {
      return Optional.of(inameToPrototype["nop"])
    }

    val prototype: InstructionPrototype?
    if (opcode == 0) {
      prototype = opcodeEquals0x00IdentifiedByFunct[machineCode.funct()]
    } else if (opcode == 0x1c) {
      prototype = opcodeEquals0x1cIdentifiedByFunct[machineCode.funct()]
    } else if (opcode == 0x01) {
      prototype = opcodeEquals0x01IdentifiedByRt[machineCode.rt()]
    } else {
      prototype = identifiedByTheirOpcodeAlone[opcode]
    }

    return Optional.ofNullable(prototype)
  }

  @JvmStatic fun allExamples(): Iterable<InstructionExample> {
    return prototypeSet.map { it.example }
  }

  /**
   * Prints the names of all the instructions contained in this set.
   * Useful for technical documentation.
   *
   * @param onlyExecutables Set to true to only print the functions
   *                        which are executable. (not yet supported)
   */
  @JvmStatic fun printInstructionSet(onlyExecutables: Boolean = false) {
    for (prototype in prototypeSet) {
      println(prototype.iname)
    }
  }
}
