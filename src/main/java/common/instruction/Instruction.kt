package common.instruction

import common.instruction.decomposedrepresentation.DecomposedRepresentation
import common.instruction.exceptions.NoSuchInstructionException
import common.instruction.parametrizedroutines.ParametrizedInstructionRoutine
import common.instruction.parametrizedroutines.iname
import common.instruction.parametrizedroutines.mnemonicEquals
import io.atlassian.fugue.Either
import java.util.*

/**
 * The {@code Instruction} class provides a unified interface for
 * instantiating {@code Instruction} instances through user-supplied
 * and intermediary representations of an Instruction.
 *
 * For example the interface accepts bare Strings, as well as the String
 * wrapper class {@code MnemonicRepresentation} that has verified that the
 * String is well-formed. This rich interface is provided for brevity,
 * especially with respect to testing and ease-of-use.
 *
 * It is the responsibility of the caller to supply valid representations
 * of the primitive representations (String & int) to the factory methods
 * that this interface supplies <i>but</i> all the methods perform a
 * best-effort attempt to instantiate a suitable {@code Instruction}.
 *
 * This means that most of the times the methods prefixed with
 * unsafe should be avoided whenever possible and the functions with
 * the monadic return-value of the
 * {@link io.atlassian.fugue.Either} class.
 *
 * An Instruction may serve as a template for another instruction,
 * i.e. we can spawn actual instances of the "add" {@code Instruction}
 * class using {@code Instruction.ADD} as a template.
 *
 * Prototype design pattern:
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
 * @property primordial A boolean flag set to true if the Instruction is
 *                  the first of its kind, existing from the beginning
 *                  of the run-time; exists solely to simplify
 *                  the internal static initialization of the class.
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
 */
data class Instruction private constructor(
      val iname: String,
      val opcode: Int,
      val mnemonicRepresentation: String,
      val numericRepresentation: Long, // Long because of overflow
      val description: String,
      val format: Format,
      val pattern: ParametrizedInstructionRoutine,
      val primordial: Boolean = true,
      var type: Type? = null,
      var rt: Int? = null,
      var funct: Int? = null) {
  val example = Example(mnemonicRepresentation, numericRepresentation)
  val decomposed = DecomposedRepresentation.fromNumber(numericRepresentation, *format.lengths)

  override fun equals(other: Any?): Boolean {
    if (other == null) return false

    when (other) {
      is Instruction -> {
        return (iname == other.iname)
              && (opcode == other.opcode)
              && (mnemonicEquals(mnemonicRepresentation,other.mnemonicRepresentation))
              && (numericRepresentation == other.numericRepresentation)
              && (format == other.format)
      }
      else -> return false
    }
  }

  operator fun invoke(mnemonicRepresentation: String, numericRepresentation: Long): Instruction {
    return this.copy(primordial=false,
          mnemonicRepresentation=mnemonicRepresentation,
          numericRepresentation=numericRepresentation)
  }

  operator fun invoke(mnemonicRepresentation : String): Instruction {
    // Need to get at the numeric representation.
    return this.pattern.invoke(this, mnemonicRepresentation)
  }

  operator fun invoke(machineCode: Long): Either<Instruction, PartiallyValidInstruction> {
    return this.pattern.invoke(this, machineCode)
  }

  init {
    if (primordial) {
      primordialSet.add(this)
    }
  }

  companion object InstructionSet {
    val primordialSet = mutableListOf<Instruction>()

    @JvmField val ADD = Instruction(
          iname = "add",
          opcode = 0,
          funct = 0x20,
          mnemonicRepresentation = "add \$t1, \$t2, \$t3",
          numericRepresentation = 0x014b4820,
          description = "Addition with overflow,. Put the" +
                " sum of registers rs and rt into register" +
                " rd. Is only valid if shamt is 0.",
          format = Format.R,
          pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)
    /*@JvmField val NOP = Instruction(
          iname = "nop",
          opcode = 0,
          funct = 0,
          mnemonicRepresentation = "nop",
          numericRepresentation = 0,
          description = "Null operation; do nothing. " +
                "Machine code is all zeroes.",
          format = Format.R,
          pattern = MnemonicPattern.NOP_PATTERN)*/
    /*@JvmField val SW = Instruction(
          iname = "sw",
          opcode = 0x2b, // 43
          mnemonicExamples = arrayOf("sw \$ra, 4(\$sp)", "sw \$a0, 0(\$sp)"),
          numericExamples = arrayOf(0xafbf0004, 0xafa40000),
          description = "Store the word from register rt at address.",
          format = Format.I,
          pattern = ::INAME_RT_RS_ADDR
    )*/

    // Lookup table
    // You can take the name of an Instruction and create
    // an Instruction of the same sort, i.e.
    // inameToPrototype["add"] yields a reference
    // to Instruction.ADD, from which other "add" instructions can
    // be derived provided you have a symbolic representation to
    // represent it.
    val inameToPrototype: HashMap<String, Instruction> = HashMap()

    /*
     * The arrays are all size 64 as there are at most 64 different
     * values to take into account.
     */
    val opcodeEquals0x00IdentifiedByFunct: Array<Instruction?>
          = Array(64, { null })
    val opcodeEquals0x01IdentifiedByRt: Array<Instruction?>
          = Array(64, { null })
    val opcodeEquals0x1cIdentifiedByFunct: Array<Instruction?>
          = Array(64, { null })
    val identifiedByTheirOpcodeAlone: Array<Instruction?>
          = Array(64, { null })

    init {
      for (prototype in primordialSet) {
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

    @Throws(NoSuchInstructionException::class)
    @JvmStatic fun from(symbolicRepresentation: String): Instruction {
      return inameToPrototype.get(symbolicRepresentation.iname())!!(symbolicRepresentation)
    }

    @Throws(NoSuchInstructionException::class)
    fun from(machineCode: Long): Either<Instruction, PartiallyValidInstruction> {
      val opcode = machineCode.opcode()

      // Check if the entire number is 0s, then we have a nop instruction
      // Once again, nop and sll clashes on the (opcode, funct) tuple so
      // we have to treat one of them as a special-case. Nop seemed easiest
      // to handle as a special case.
      /*if (machineCode.equals(0)) {
        return Either.left(NOP)
      }*/

      val inst: Instruction?
      if (opcode == 0) {
        inst = opcodeEquals0x00IdentifiedByFunct[machineCode.funct()]
      } else if (opcode == 0x1c) {
        inst = opcodeEquals0x1cIdentifiedByFunct[machineCode.funct()]
      } else if (opcode == 0x01) {
        inst = opcodeEquals0x01IdentifiedByRt[machineCode.rt()]
      } else {
        inst = identifiedByTheirOpcodeAlone[opcode]
      }

      if (inst == null) {
        throw NoSuchInstructionException(machineCode)
      }

      return Either.left(inst)
    }

    @JvmStatic fun allExamples(): Iterable<Example> {
      return primordialSet.map { it.example }
    }

    /**
     * Prints the names of all the instructions contained in this set.
     * Useful for technical documentation.
     *
     * @param onlyExecutables Set to true to only print the functions
     *                        which are executable. (not yet supported)
     */
    @JvmStatic fun printInstructionSet(onlyExecutables: Boolean = false) {
      for (prototype in primordialSet) {
        println(prototype.iname)
      }
    }
  }
}


