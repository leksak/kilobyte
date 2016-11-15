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
 * @property mnemonicRepresentation A symbolic representation of the instruction instance
 * @property numericRepresentation A numeric representation of <i>the same</i>
 *                       instruction instance as the mnemonicRepresentation property
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

  fun asPaddedHexString(): String {
    // Pad the string with leading zeroes. Target length is 10
    // characters (including 0x). So for instance, the number
    // 0x3e00008
    // should be written as
    // 0x03e00008

    // Creates a hexadecimal string without the 0x prefix, target
    // length is 8 characters.
    var hexString = Integer.toHexString(numericRepresentation.toInt())

    while (hexString.length < 8) {
      hexString = "0" + hexString
    }

    return "0x" + hexString
  }

  fun asDecimalString() = decomposed.asDecimalString()
  fun asHexadecimalString() = decomposed.asHexadecimalString()

  override fun toString(): String {
    return "%s %s %s %s %s".format(
            asPaddedHexString(),
            format,
            asDecimalString(),
            asHexadecimalString(),
            mnemonicRepresentation
            )
  }

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

  override fun hashCode(): Int {
    var result = iname.hashCode()
    result = 31 * result + opcode
    result = 31 * result + mnemonicRepresentation.hashCode()
    result = 31 * result + numericRepresentation.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + format.hashCode()
    result = 31 * result + pattern.hashCode()
    result = 31 * result + primordial.hashCode()
    result = 31 * result + (type?.hashCode() ?: 0)
    result = 31 * result + (rt ?: 0)
    result = 31 * result + (funct ?: 0)
    result = 31 * result + example.hashCode()
    result = 31 * result + (decomposed?.hashCode() ?: 0)
    return result
  }

  init {
    if (primordial) {
      primordialSet.add(this)
    }
  }

  companion object InstructionSet {
    val primordialSet = mutableListOf<Instruction>()


    @JvmField val NOP = Instruction(
            iname = "nop",
            opcode = 0,
            funct = 0,
            mnemonicRepresentation = "nop",
            numericRepresentation = 0,
            description = "Null operation; do nothing. " +
                    "Machine code is all zeroes.",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.NOP)

    @JvmField val SLL = Instruction(
            iname = "sll",
            opcode = 0,
            funct = 0,
            mnemonicRepresentation = "sll \$t1, \$t2, \$10",
            numericRepresentation = 0x014A4800,
            description = "Shift left logical : Set \$t1 to result of " +
                    "shifting \$t2 left by number of bits specified by " +
                    "immediate",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SRL = Instruction(
            iname = "srl",
            opcode = 0,
            funct = 2,
            mnemonicRepresentation = "srl \$t1, \$t2, \$10",
            numericRepresentation = 0x014A4802,
            description = "Shift right logical : Set \$t1 to result of " +
                    "shifting \$t2 right by number of bits specified " +
                    "by immediate",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SRA = Instruction(
            iname = "sra",
            opcode = 0,
            funct = 3,
            mnemonicRepresentation = "sra \$t1, \$t2, \$10",
            numericRepresentation = 0x014A4803,
            description = "Shift right arithmetic : Set \$t1 to result of " +
                    "sign-extended shifting \$t2 right by number of bits " +
                    "specified by immediateShift right arithmetic : Set \$t1 to " +
                    "result of sign-extended shifting \$t2 right by number of " +
                    "bits specified by immediate",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SLLV = Instruction(
            iname = "sllv",
            opcode = 0,
            funct = 4,
            mnemonicRepresentation = "sllv \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b4804,
            description = "Shift left logical variable : Set \$t1 to result " +
                    "of shifting \$t2 left by number of bits specified by " +
                    "value in low-order 5 bits of \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SRLV = Instruction(
            iname = "srlv",
            opcode = 0,
            funct = 6,
            mnemonicRepresentation = "srlv \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b4806,
            description = "Shift right logical variable : Set \$t1 to result " +
                    "of shifting \$t2 right by number of bits specified by " +
                    "value in low-order 5 bits of \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SRAV = Instruction(
            iname = "srav",
            opcode = 0,
            funct = 7,
            mnemonicRepresentation = "srav \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b4807,
            description = "Shift right arithmetic variable : Set \$t1 to " +
                    "result of sign-extended shifting \$t2 right by number " +
                    "of bits specified by value in low-order 5 bits of \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val JR = Instruction(
            iname = "jr",
            opcode = 0,
            funct = 8,
            mnemonicRepresentation = "jr \$t1",
            numericRepresentation = 0x01200008,
            description = "Jump register unconditionally : Jump to statement " +
                    "whose address is in \$t1",
            format = Format.J,
            pattern = ParametrizedInstructionRoutine.INAME_RS)

    //TODO: Also exist JALR $t1 ($zero) but this should suffice.
    @JvmField val JALR = Instruction(
            iname = "jalr",
            opcode = 0,
            funct = 9,
            mnemonicRepresentation = "jalr \$t1, \$t2",
            numericRepresentation = 0x01404809,
            description = "Jump and link register : Set \$t1 to Program Counter " +
                    "(return address) then jump to statement whose address is " +
                    "in \$t2",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS)

    @JvmField val MOVZ = Instruction(
            iname = "movz",
            opcode = 0,
            funct = 10,
            mnemonicRepresentation = "movz \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b480a,
            description = "Move conditional zero : Set \$t1 to \$t2 if " +
                    "\$t3 is zero",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val MOVN = Instruction(
            iname = "movn",
            opcode = 0,
            funct = 11,
            mnemonicRepresentation = "movn \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b480b,
            description = "Move conditional not zero : Set \$t1 to \$t2 " +
                    "if \$t3 is not zero",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SYSCALL = Instruction(
            iname = "syscall",
            opcode = 0,
            funct = 12,
            mnemonicRepresentation = "syscall",
            numericRepresentation = 0x0000000c,
            description = "Issue a system call : Execute the system call " +
                    "specified by value in \$v0",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME)

    @JvmField val BREAK = Instruction(
            iname = "break",
            opcode = 0,
            funct = 13,
            mnemonicRepresentation = "break",
            numericRepresentation = 0x0000000d,
            description = "Break execution : Terminate program execution " +
                    "with exception",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME)

    @JvmField val SYNC = Instruction(
            iname = "sync",
            opcode = 0,
            funct = 15,
            mnemonicRepresentation = "sync",
            numericRepresentation = 0x0000000f,
            description = "To order loads and stores to shared memory in a " +
                    "multiprocessor system",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME)

    //TODO: numeric mismatch? (0x00004810)Probably weird since only copy to register
    @JvmField val MFHI = Instruction(
            iname = "mfhi",
            opcode = 0,
            funct = 16,
            mnemonicRepresentation = "mfhi \$t1",
            numericRepresentation = 0x00004810,
            description = "Move from HI register : Set \$t1 to contents of " +
                    "HI (see multiply and divide operations)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD)

    //TODO: numeric mismatch? (0x00004811)Probably weird since only copy to register
    @JvmField val MTHI = Instruction(
            iname = "mthi",
            opcode = 0,
            funct = 17,
            mnemonicRepresentation = "mthi \$t1",
            numericRepresentation = 0x01200011,
            description = "Move to HI registerr : Set HI to contents of " +
                    "\$t1 (see multiply and divide operations)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS)
    //TODO: numeric mismatch? (0x00004812)Probably weird since only copy to register
    @JvmField val MFLO = Instruction(
            iname = "mflo",
            opcode = 0,
            funct = 18,
            mnemonicRepresentation = "mflo \$t1",
            numericRepresentation = 0x00004812,
            description = "Move from LO register : Set \$t1 to contents of " +
                    "LO (see multiply and divide operations)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD)


    //TODO: numeric mismatch? (0x00004813)Probably weird since only copy to register
    @JvmField val MTLO = Instruction(
            iname = "mtlo",
            opcode = 0,
            funct = 19,
            mnemonicRepresentation = "mtlo \$t1",
            numericRepresentation = 0x01200013,
            description = "Move to LO register : Set LO to contents of " +
                    "\$t1 (see multiply and divide operations)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS)

    @JvmField val MULT = Instruction(
            iname = "mult",
            opcode = 0,
            funct = 24,
            mnemonicRepresentation = "mult \$t1, \$t2",
            numericRepresentation = 0x012A0018,
            description = "ultiplication : Set hi to high-order 32 bits, " +
                    "lo to low-order 32 bits of the product of \$t1 and " +
                    "\$t2 (use mfhi to access hi, mflo to access lo)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)

    @JvmField val MULTU = Instruction(
            iname = "multu",
            opcode = 0,
            funct = 25,
            mnemonicRepresentation = "multu \$t1, \$t2",
            numericRepresentation = 0x012A0019,
            description = "Multiplication unsigned : Set HI to high-order " +
                    "32 bits, LO to low-order 32 bits of the product of " +
                    "unsigned \$t1 and \$t2 (use mfhi to access HI, " +
                    "mflo to access LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)

    @JvmField val DIV = Instruction(
            iname = "div",
            opcode = 0,
            funct = 26,
            mnemonicRepresentation = "div \$t1, \$t2",
            numericRepresentation = 0x012A001A,
            description = " Division with overflow : Divide \$t1 by " +
                    "\$t2 then set LO to quotient and HI to remainder " +
                    "(use mfhi to access HI, mflo to access LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)


    @JvmField val DIVU = Instruction(
            iname = "divu",
            opcode = 0,
            funct = 27,
            mnemonicRepresentation = "divu \$t1, \$t2",
            numericRepresentation = 0x012A001B,
            description = "Division unsigned without overflow : Divide " +
                    "unsigned \$t1 by \$t2 then set LO to quotient and " +
                    "HI to remainder (use mfhi to access HI, mflo to access " +
                    "LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)

    @JvmField val ADD = Instruction(
            iname = "add",
            opcode = 0,
            funct = 32,
            mnemonicRepresentation = "add \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b4820,
            description = "Addition with overflow,. Put the" +
                    " sum of registers rs and rt into register" +
                    " rd. Is only valid if shamt is 0.",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val ADDU = Instruction(
            iname = "addu",
            opcode = 0,
            funct = 33,
            mnemonicRepresentation = "addu \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B4821,
            description = "Addition unsigned without overflow : set \$t1 to " +
                    "(\$t2 plus \$t3), no overflow",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SUB = Instruction(
            iname = "sub",
            opcode = 0,
            funct = 34,
            mnemonicRepresentation = "sub \$t1, \$t2, \$t3",
            numericRepresentation = 0x014b4822,
            description = "Subtraction with overflow : " +
                    "set \$t1 to (\$t2 minus \$t3)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SUBU = Instruction(
            iname = "subu",
            opcode = 0,
            funct = 35,
            mnemonicRepresentation = "subu \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B4823,
            description = "Subtraction unsigned without overflow : set " +
                    "\$t1 to (\$t2 minus \$t3), no overflow",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val AND = Instruction(
            iname = "and",
            opcode = 0,
            funct = 36,
            mnemonicRepresentation = "and \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B4824,
            description = "Bitwise AND : Set \$t1 to bitwise " +
                    "AND of \$t2 and \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val OR = Instruction(
            iname = "or",
            opcode = 0,
            funct = 37,
            mnemonicRepresentation = "or \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B4825,
            description = "Bitwise OR : Set \$t1 to bitwise OR of \$t2 " +
                    "and \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val XOR = Instruction(
            iname = "xor",
            opcode = 0,
            funct = 38,
            mnemonicRepresentation = "xor \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B4826,
            description = "Bitwise XOR (exclusive OR) : Set \$t1 to bitwise " +
                    "XOR of \$t2 and \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val NOR = Instruction(
            iname = "nor",
            opcode = 0,
            funct = 39,
            mnemonicRepresentation = "nor \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B4827,
            description = "Bitwise NOR : Set \$t1 to bitwise NOR of \$t2 " +
                    "and \$t3",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SLT = Instruction(
            iname = "slt",
            opcode = 0,
            funct = 42,
            mnemonicRepresentation = "slt \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B482A,
            description = "Set less than : If \$t2 is less than \$t3, then " +
                    "set \$t1 to 1 else set \$t1 to 0",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val SLTU = Instruction(
            iname = "sltu",
            opcode = 0,
            funct = 43,
            mnemonicRepresentation = "sltu \$t1, \$t2, \$t3",
            numericRepresentation = 0x014B482B,
            description = "Set less than unsigned : If \$t2 is less than " +
                    "\$t3 using unsigned comparision, then set \$t1 to 1 " +
                    "else set \$t1 to 0",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val TGE = Instruction(
            iname = "tge",
            opcode = 0,
            funct = 48,
            mnemonicRepresentation = "tge \$t1, \$t2",
            numericRepresentation = 0x012A0030,
            description = "Trap if greater or equal : Trap if \$t1 is " +
                    "greater than or equal to \$t2",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)

    @JvmField val TGEU = Instruction(
            iname = "tgeu",
            opcode = 0,
            funct = 49,
            mnemonicRepresentation = "tgeu \$t1, \$t2",
            numericRepresentation = 0x012A0031,
            description = "Trap if greater or equal : Trap if \$t1 is " +
                    "greater than or equal to \$t2",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)

    @JvmField val TLT = Instruction(
            iname = "tlt",
            opcode = 0,
            funct = 50,
            mnemonicRepresentation = "tlt \$t1, \$t2",
            numericRepresentation = 0x012A0032,
            description = "Trap if less than: Trap if \$t1 less than \$t2",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)

    @JvmField val TLTU = Instruction(
            iname = "tltu",
            opcode = 0,
            funct = 51,
            mnemonicRepresentation = "tltu \$t1, \$t2",
            numericRepresentation = 0x012A0033,
            description = "Trap if less than unsigned : Trap if \$t1 less " +
                    "than \$t2, unsigned comparison",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)


    @JvmField val TEQ = Instruction(
            iname = "teq",
            opcode = 0,
            funct = 52,
            mnemonicRepresentation = "teq \$t1, \$t2",
            numericRepresentation = 0x012A0034,
            description = "Trap if equal : Trap if \$t1 is equal to \$t2",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)


    @JvmField val TNE = Instruction(
            iname = "tne",
            opcode = 0,
            funct = 54,
            mnemonicRepresentation = "tne \$t1, \$t2",
            numericRepresentation = 0x012A0036,
            description = "Trap if not equal : Trap if \$t1 is not " +
                    "equal to \$t2",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RS_RT)


    /* Opcode 0x1c(16) 28(10) 011100(2)*/
    /*
    @JvmField val MADD = Instruction(
            iname = "madd",
            opcode = 28,
            funct = 0,
            mnemonicRepresentation = "madd \$t1, \$t2",
            //TODO: numericRepresentation
            numericRepresentation = 0,
            description = "Multiply add : Multiply \$t1 by \$t2 then " +
                    "increment HI by high-order 32 bits of product, " +
                    "increment LO by low-order 32 bits of product (use mfhi " +
                    "to access HI, mflo to access LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)
    @JvmField val MADDU = Instruction(
            iname = "maddu",
            opcode = 0x1c,
            funct = 1,
            mnemonicRepresentation = "maddu \$t1, \$t2",
            //TODO: numericRepresentation
            numericRepresentation = 0,
            description = "Multiply add unsigned : Multiply \$t1 by \$t2 " +
                    "then increment HI by high-order 32 bits of product, " +
                    "increment LO by low-order 32 bits of product, " +
                    "unsigned (use mfhi to access HI, mflo to access LO",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val MUL = Instruction(
            iname = "mul",
            opcode = 0x1c,
            funct = 2,
            mnemonicRepresentation = "mul \$v0, \$a0, \$v0",
            //TODO: numericRepresentation
            numericRepresentation = 0x70821002,
            description = "Multiplication without overflow  : Set HI to " +
                    "high-order 32 bits, LO and \$t1 to low-order 32 bits of " +
                    "the product of \$t2 and \$t3 (use mfhi to access HI, mflo " +
                    "to access LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val MSUB = Instruction(
            iname = "msub",
            opcode = 0x1c,
            funct = 4,
            mnemonicRepresentation = "msub \$t1, \$t2",
            //TODO: numericRepresentation
            numericRepresentation = 0,
            description = "Multiply subtract : Multiply \$t1 by \$t2 then " +
                    "decrement HI by high-order 32 bits of product, " +
                    "decrement LO by low-order 32 bits of product (use mfhi " +
                    "to access HI, mflo to access LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)

    @JvmField val MSUBU = Instruction(
            iname = "msubu",
            opcode = 0x1c,
            funct = 5,
            mnemonicRepresentation = "msubu \$t1, \$t2",
            //TODO: numericRepresentation
            numericRepresentation = 0,
            description = "Multiply subtract unsigned : Multiply \$t1 by \$t2" +
                    " then decrement HI by high-order 32 bits of product, " +
                    "decement LO by low-order 32 bits of product, unsigned " +
                    "(use mfhi to access HI, mflo to access LO)",
            format = Format.R,
            pattern = ParametrizedInstructionRoutine.INAME_RD_RS_RT)


    /*@JvmField val SW = Instruction(
          iname = "sw",
          opcode = 0x2b, // 43
          mnemonicRepresentation = "sw \$ra, 4(\$sp)",
          numericRepresentation = 0xafbf0004,
          description = "Store the word from register rt at address.",
          format = Format.I,
          pattern = ParametrizedInstructionRoutine.INAME_RT_RS_ADDR*/

    */

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
      val iname = symbolicRepresentation.iname()
      if (!inameToPrototype.containsKey(iname)) {
        throw NoSuchInstructionException(iname)
      }
      return inameToPrototype[symbolicRepresentation.iname()]!!(symbolicRepresentation)
    }

    @Throws(NoSuchInstructionException::class)
    @JvmStatic fun unsafeFrom(machineCode: Long): Instruction {
      return from(machineCode).left().get()
    }

    @Throws(NoSuchInstructionException::class)
    @JvmStatic fun from(machineCode: Long): Either<Instruction, PartiallyValidInstruction> {
      val opcode: Int = machineCode.opcode().toInt()

      // Check if the entire number is 0s, then we have a nop instruction
      // Once again, nop and sll clashes on the (opcode, funct) tuple so
      // we have to treat one of them as a special-case. Nop seemed easiest
      // to handle as a special case.
      if (machineCode == Integer.toUnsignedLong(0)) {
        return Either.left(NOP)
      }

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


