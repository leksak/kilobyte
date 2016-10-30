package common.instruction

import java.util.*
import java.util.Optional.*

/**
 * An InstructionPrototype can serve as a template for another instruction,
 * i.e. we can spawn actual instances of the "add" {@code Instruction}
 * class using {@code InstructionPrototype.ADD} as a template.
 * 
 * This is a novel take on the Prototype design pattern:
 * http://gameprogrammingpatterns.com/prototype.html
 *
 * Developers Note:
 *
 * This class was written in Kotlin as it supports named arguments
 * out of the box. This can be mimicked in Java using a Builder
 * pattern but that generates a lot of boilerplate even when
 * using libraries such as Lombok. Also, the Builder pattern is
 * less legible as it is not supported on the language level.
 *
 * By using a language that supports named arguments instead we can
 * alleviate ourselves of a large, verbose and unnecessarily complex
 * code-base. However, we use a single constructor to encompass all
 * different ways of instantiating an instruction, thereby we end
 * up letting a lot of attributes that could effectively be final
 * as being variable. By reducing the visibility of the constructor
 * to the private level we retain security on the API level while
 * striking a good balance in brevity and legibility.
 *
 * Written in Kotlin because of named arguments as there is a
 * cornucopia of ways to instantiate an instruction.
 *
 * The constructor is hidden since only we require it. By limiting
 * its visibility we may define a single constructor with multiple
 * "var" arguments instead of having a cornucopia of constructors.
 *
 * Hence, we trade-off API usage security for brevity, as the usage
 * of this class is very limited.
 *
 * For more information about named arguments refer to the
 * Kotlin documentation available here
 * https://kotlinlang.org/docs/reference/functions.html#named-arguments
 *
 * @property iname The name of the instruction
 * @property opcode The opcode of the instruction
 * @property mnemonicExample A symbolic representation of one instruction instance
 * @property numericExample A numeric representation of <i>the same</i>
 *                       instruction instance as the mnemonicExample property
 * @property description A description of the semantics of the instruction
 * @property format The format of the instruction
 * @property pattern The pattern that the symbolic representation of the
 *                instruction adheres to. For an example, the "add"
 *                instruction is on the form (iname, rd, rs, rt) meaning
 *                that for the instruction instance "add $t1, $t2, $t3"
 *                we have that rd=$t1, rs=$t2, rt=$t3
 * @property type The type of the instruction, if applicable.
 * @property rt If the instruction is identified by the value of its
 *                opcode and the value of the rt field then identRt
 *                should be set to the the same value.
 * @property funct Like the @rt property but for the funct field.
 * @property conditions If applicable: A set of conditions that need to
 *                   apply for a numeric representation of the instruction
 *                   to be valid.
 */
class InstructionPrototype constructor(
      val iname: String,
      val opcode: Int,
      val mnemonicExample: String,
      val numericExample: Int,
      val description: String,
      val format: Format,
      val pattern: Pattern,
      var type: Type? = null,
      var rt: Int? = null,
      var funct: Int? = null,
      vararg var conditions: Condition = emptyArray()) {
  val example = InstructionExample(mnemonicExample, numericExample)
  val mnemonic = MnemonicRepresentation(mnemonicExample)

  fun asInstruction() = Instruction(iname, numericExample, mnemonic, this)
  
  companion object InstructionSet {
    val shamt_is_zero: Condition = Condition(
          {it -> if (it.shamt() == 0) {
            ConditionResult.Success()
          } else {
            ConditionResult.Failure("Shamt has to be zero. Got: " + it)
          }}
    )

    @JvmField val ADD = InstructionPrototype(
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

    val prototypeSet: Array<InstructionPrototype> =
          arrayOf(ADD)

    // Lookup table
    // You can take the name of an InstructionPrototype and create
    // an Instruction of the same sort, i.e. 
    // inameToPrototype["add"] yields a reference
    // to InstructionPrototype.ADD, from which other "add" instructions can
    // be derived provided you have a symbolic representation to
    // represent it.
    val inameToPrototype: HashMap<String, InstructionPrototype> = HashMap()
    val opcodeEquals0x00IdentifiedByFunct: Array<InstructionPrototype?>
          = Array(64, { null })
    val opcodeEquals0x01IdentifiedByRt: Array<InstructionPrototype?>
          = Array(64, { null })
    val opcodeEquals0x1cIdentifiedByFunct: Array<InstructionPrototype?>
          = Array(64, { null })
    val identifiedByTheirOpcodeAlone: Array<InstructionPrototype?>
          = Array(64, { null })

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

    @JvmStatic fun get(iname: String) = ofNullable(unsafeGet(iname))
    @JvmStatic fun get(machineCode: Int) = ofNullable(unsafeGet(machineCode))
    @JvmStatic fun unsafeGet(iname: String) = inameToPrototype[iname]

    @JvmStatic fun unsafeGet(machineCode: Int): InstructionPrototype? {
      val opcode = machineCode.opcode()

      // Check if the entire number is 0s, then we have a nop instruction
      // Once again, nop and sll clashes on the (opcode, funct) tuple so
      // we have to treat one of them as a special-case. Nop seemed easiest
      // to handle as a special case.
      if (machineCode == 0x00) {
        return inameToPrototype["nop"]
      }

      if (opcode == 0) {
        return opcodeEquals0x00IdentifiedByFunct[machineCode.funct()]
      } else if (opcode == 0x1c) {
        return opcodeEquals0x1cIdentifiedByFunct[machineCode.funct()]
      } else if (opcode == 0x01) {
        return opcodeEquals0x01IdentifiedByRt[machineCode.rt()]
      } else {
        return identifiedByTheirOpcodeAlone[opcode]
      }  
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
}

