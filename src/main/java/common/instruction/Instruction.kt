package common.instruction

import common.instruction.decomposedrepresentation.DecomposedRepresentation
import common.instruction.exceptions.NoSuchInstructionException
import common.instruction.parametrizedroutines.*

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
  var funct: Int? = null,
  var offset: Int? = null,
  var hint: Hint? = null) {
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
      numericRepresentation=Integer.toUnsignedLong(numericRepresentation.toInt()))
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
    result = 31 * result + (offset ?: 0)
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

    /* OP-code: 0(_10) 0x0(_16) 000000(_2) */
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
      pattern = INAME_RD_RS_RT)

    //TODO: movf movt

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
      pattern = INAME_RD_RS_RT)

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
      pattern = INAME_RD_RS_RT)

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
      pattern = INAME_RD_RS_RT)

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
      pattern = INAME_RD_RS_RT)

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
      pattern = INAME_RD_RS_RT)

    @JvmField val JR = Instruction(
      iname = "jr",
      opcode = 0,
      funct = 8,
      mnemonicRepresentation = "jr \$t1",
      numericRepresentation = 0x01200008,
      description = "Jump register unconditionally : Jump to statement " +
        "whose offset is in \$t1",
      format = Format.R,
      type=Type.J,
      pattern = INAME_RS)

    //TODO: Also exist JALR $t1 ($zero) but this should suffice.
    @JvmField val JALR = Instruction(
      iname = "jalr",
      opcode = 0,
      funct = 9,
      mnemonicRepresentation = "jalr \$t1, \$t2",
      numericRepresentation = 0x01404809,
      description = "Jump and link register : Set \$t1 to Program Counter " +
        "(return offset) then jump to statement whose offset is " +
        "in \$t2",
      format = Format.R,
      type = Type.J,
      pattern = INAME_RD_RS)

    @JvmField val MOVZ = Instruction(
      iname = "movz",
      opcode = 0,
      funct = 10,
      mnemonicRepresentation = "movz \$t1, \$t2, \$t3",
      numericRepresentation = 0x014b480a,
      description = "Move conditional zero : Set \$t1 to \$t2 if " +
        "\$t3 is zero",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val MOVN = Instruction(
      iname = "movn",
      opcode = 0,
      funct = 11,
      mnemonicRepresentation = "movn \$t1, \$t2, \$t3",
      numericRepresentation = 0x014b480b,
      description = "Move conditional not zero : Set \$t1 to \$t2 " +
        "if \$t3 is not zero",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    // TODO: The following three aren't actually in the R-format.
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

    @JvmField val MFHI = Instruction(
      iname = "mfhi",
      opcode = 0,
      funct = 16,
      mnemonicRepresentation = "mfhi \$t1",
      numericRepresentation = 0x00004810,
      description = "Move from HI register : Set \$t1 to contents of " +
        "HI (see multiply and divide operations)",
      format = Format.R,
      pattern = INAME_RD)

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

    @JvmField val MFLO = Instruction(
      iname = "mflo",
      opcode = 0,
      funct = 18,
      mnemonicRepresentation = "mflo \$t1",
      numericRepresentation = 0x00004812,
      description = "Move from LO register : Set \$t1 to contents of " +
        "LO (see multiply and divide operations)",
      format = Format.R,
      pattern = INAME_RD)

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
      description = "Multiplication : Set hi to high-order 32 bits, " +
        "lo to low-order 32 bits of the product of \$t1 and " +
        "\$t2 (use mfhi to access hi, mflo to access lo)",
      format = Format.R,
      pattern = INAME_RS_RT)

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
      pattern = INAME_RS_RT)

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
      pattern = INAME_RS_RT)

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
      pattern = INAME_RS_RT)

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
      pattern = INAME_RD_RS_RT)

    @JvmField val ADDU = Instruction(
      iname = "addu",
      opcode = 0,
      funct = 33,
      mnemonicRepresentation = "addu \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B4821,
      description = "Addition unsigned without overflow : set \$t1 to " +
        "(\$t2 plus \$t3), no overflow",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val SUB = Instruction(
      iname = "sub",
      opcode = 0,
      funct = 34,
      mnemonicRepresentation = "sub \$t1, \$t2, \$t3",
      numericRepresentation = 0x014b4822,
      description = "Subtraction with overflow : " +
        "set \$t1 to (\$t2 minus \$t3)",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val SUBU = Instruction(
      iname = "subu",
      opcode = 0,
      funct = 35,
      mnemonicRepresentation = "subu \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B4823,
      description = "Subtraction unsigned without overflow : set " +
        "\$t1 to (\$t2 minus \$t3), no overflow",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val AND = Instruction(
      iname = "and",
      opcode = 0,
      funct = 36,
      mnemonicRepresentation = "and \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B4824,
      description = "Bitwise AND : Set \$t1 to bitwise " +
        "AND of \$t2 and \$t3",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val OR = Instruction(
      iname = "or",
      opcode = 0,
      funct = 37,
      mnemonicRepresentation = "or \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B4825,
      description = "Bitwise OR : Set \$t1 to bitwise OR of \$t2 " +
        "and \$t3",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val XOR = Instruction(
      iname = "xor",
      opcode = 0,
      funct = 38,
      mnemonicRepresentation = "xor \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B4826,
      description = "Bitwise XOR (exclusive OR) : Set \$t1 to bitwise " +
        "XOR of \$t2 and \$t3",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val NOR = Instruction(
      iname = "nor",
      opcode = 0,
      funct = 39,
      mnemonicRepresentation = "nor \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B4827,
      description = "Bitwise NOR : Set \$t1 to bitwise NOR of \$t2 " +
        "and \$t3",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val SLT = Instruction(
      iname = "slt",
      opcode = 0,
      funct = 42,
      mnemonicRepresentation = "slt \$t1, \$t2, \$t3",
      numericRepresentation = 0x014B482A,
      description = "Set less than : If \$t2 is less than \$t3, then " +
        "set \$t1 to 1 else set \$t1 to 0",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

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
      pattern = INAME_RD_RS_RT)

    @JvmField val TGE = Instruction(
      iname = "tge",
      opcode = 0,
      funct = 48,
      mnemonicRepresentation = "tge \$t1, \$t2",
      numericRepresentation = 0x012A0030,
      description = "Trap if greater or equal : Trap if \$t1 is " +
        "greater than or equal to \$t2",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val TGEU = Instruction(
      iname = "tgeu",
      opcode = 0,
      funct = 49,
      mnemonicRepresentation = "tgeu \$t1, \$t2",
      numericRepresentation = 0x012A0031,
      description = "Trap if greater or equal : Trap if \$t1 is " +
        "greater than or equal to \$t2",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val TLT = Instruction(
      iname = "tlt",
      opcode = 0,
      funct = 50,
      mnemonicRepresentation = "tlt \$t1, \$t2",
      numericRepresentation = 0x012A0032,
      description = "Trap if less than: Trap if \$t1 less than \$t2",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val TLTU = Instruction(
      iname = "tltu",
      opcode = 0,
      funct = 51,
      mnemonicRepresentation = "tltu \$t1, \$t2",
      numericRepresentation = 0x012A0033,
      description = "Trap if less than unsigned : Trap if \$t1 less " +
        "than \$t2, unsigned comparison",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val TEQ = Instruction(
      iname = "teq",
      opcode = 0,
      funct = 52,
      mnemonicRepresentation = "teq \$t1, \$t2",
      numericRepresentation = 0x012A0034,
      description = "Trap if equal : Trap if \$t1 is equal to \$t2",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val TNE = Instruction(
      iname = "tne",
      opcode = 0,
      funct = 54,
      mnemonicRepresentation = "tne \$t1, \$t2",
      numericRepresentation = 0x012A0036,
      description = "Trap if not equal : Trap if \$t1 is not " +
        "equal to \$t2",
      format = Format.R,
      pattern = INAME_RS_RT)



    /* OP-code: 1(_10) 0x01(_16) 000001(_2) */
    @JvmField val BLTZ = Instruction(
      iname = "bltz",
      opcode = 1,
      rt = 0,
      mnemonicRepresentation = "bltz \$t1, 5",
      numericRepresentation = 0x05200005,
      description = "Branch if less than zero : Branch to statement at " +
        "label's address if \$t1 is less than zero",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BGEZ = Instruction(
      iname = "bgez",
      opcode = 1,
      rt = 1,
      mnemonicRepresentation = "bgez \$t1, 5",
      numericRepresentation = 0x05210005,
      description = "Branch if greater than or equal to zero : Branch to " +
        "statement at label's address if \$t1 is greater than or equal to zero",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BLTZL = Instruction(
      iname = "bltzl",
      opcode = 1,
      rt = 2,
      mnemonicRepresentation = "bltzl \$t1, 5",
      numericRepresentation = 0x05220005,
      description = "Branch if less than zero likely : Branch to statement at " +
        "label's address if \$t1 is less than zero",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BGEZL = Instruction(
      iname = "bgezl",
      opcode = 1,
      rt = 3,
      mnemonicRepresentation = "bgezl \$t1, 5",
      numericRepresentation = 0x05230005,
      description = "Branch if greater than or equal to zero likely: " +
        "Branch to statement at label's address if \$t1 is greater than or " +
        "equal to zero",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val TGEI = Instruction(
      iname = "tgei",
      opcode = 1,
      rt = 8,
      mnemonicRepresentation = "tgei \$t1, 5",
      numericRepresentation = 0x05280005,
      description = "Trap if greater than or equal to immediate : " +
        "Trap if \$t1 greater than or equal to sign-extended 16 bit immediate",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val TGEIU = Instruction(
      iname = "tgeiu",
      opcode = 1,
      rt = 9,
      mnemonicRepresentation = "tgeiu \$t1, 5",
      numericRepresentation = 0x05290005,
      description = "Trap if greater or equal to immediate unsigned : " +
        "Trap if \$t1 greater than or equal to sign-extended 16 bit " +
        "immediate, unsigned comparison",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val TLTI = Instruction(
      iname = "tlti",
      opcode = 1,
      rt = 10,
      mnemonicRepresentation = "tlti \$t1, 5",
      numericRepresentation = 0x052A0005,
      description = "Trap if less than immediate : Trap if \$t1 less than " +
        "sign-extended 16-bit immediate",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val TLTIU = Instruction(
      iname = "tltiu",
      opcode = 1,
      rt = 11,
      mnemonicRepresentation = "tltiu \$t1, 5",
      numericRepresentation = 0x052B0005,
      description = "Trap if less than immediate unsigned : Trap if \$t1 " +
        "less than sign-extended 16-bit immediate, unsigned comparison",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val TEQI = Instruction(
      iname = "teqi",
      opcode = 1,
      rt = 12,
      mnemonicRepresentation = "teqi \$t1, 5",
      numericRepresentation = 0x052C0005,
      description = "Trap if equal to immediate : Trap if \$t1 " +
        "is equal to sign-extended 16 bit immediate",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val TNEI = Instruction(
      iname = "tnei",
      opcode = 1,
      rt = 14,
      mnemonicRepresentation = "tnei \$t1, 5",
      numericRepresentation = 0x052E0005,
      description = "Trap if not equal to immediate: " +
        "Trap if \$t1 is not equal to sign-extended 16 bit immediate",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BLTZAL = Instruction(
      iname = "bltzal",
      opcode = 1,
      rt = 16,
      mnemonicRepresentation = "bltzal \$t1, 10",
      numericRepresentation = 0x0530000A,
      description = "Branch if less than zero and link : If \$t1 is less " +
        "than or equal to zero, then set \$ra to the Program Counter and " +
        "branch to statement at label's address",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BGEZAL = Instruction(
      iname = "bgezal",
      opcode = 1,
      rt = 17,
      mnemonicRepresentation = "bgezal \$t1, 10",
      numericRepresentation = 0x0531000A,
      description = "Branch if greater then or equal to zero and link: " +
        "If \$t1 is greater than or equal to zero, then set \$ra to the " +
        "Program Counter and branch to statement at label's address",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BLTZALL = Instruction(
      iname = "bltzall",
      opcode = 1,
      rt = 18,
      mnemonicRepresentation = "bltzall \$t1, 10",
      numericRepresentation = 0x0532000A,
      description = "Branch on Less Than Zero And Link Likely: " +
        "If \$t1 is greater than or equal to zero, then set \$ra to the " +
        "Program Counter and branch to statement at label's address",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BGCZALL = Instruction(
      iname = "bgczall",
      opcode = 1,
      rt = 19,
      mnemonicRepresentation = "bgczall \$t1, 10",
      numericRepresentation = 0x0533000A,
      description = "Branch on Greater Than or Equal to Zero and Link Likely:" +
        "if (rs >= 0) then procedure_call_likely Place the return address " +
        "link in GPR 31. The return link is the address of the second " +
        "instruction following the branch, where execution would continue " +
        "after a procedure call. An 18-bit signed offset (the 16-bit offset " +
        "field shifted left 2 bits) is added to the address of the " +
        "instruction following the branch (not the branch itself), in the " +
        "branch delay slot, to form a PC-relative effective target address. " +
        "If the contents of GPR rs are greater than or equal to zero " +
        "(sign bit is 0), branch to the effective target address after the " +
        "instruction in the delay slot is executed. If the branch is " +
        "not taken, the instruction in the delay slot is not executed.",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    /* OP-code: 2(_10) 0x02(_16) 000010(_2) */
    @JvmField val J = Instruction(
      iname = "j",
      opcode = 2, //0x02
      mnemonicRepresentation = "j 4",
      numericRepresentation = 0x08000004,
      description = "Jump unconditionally : Jump to statement at target address",
      format = Format.J,
      pattern = INAME_TARGET)

    @JvmField val JAL = Instruction(
      iname = "jal",
      opcode = 3, //0x03
      mnemonicRepresentation = "jal 4",
      numericRepresentation = 0x0c000004,
      description = "Jump and link : Set \$ra to Program Counter " +
        "(return address) then jump to statement at target address",
      format = Format.J,
      pattern = INAME_TARGET)

    @JvmField val BEQ = Instruction(
      iname = "beq",
      opcode = 4, //0x04
      mnemonicRepresentation = "beq \$t1, \$t2, 4",
      numericRepresentation = 0x112A0004,
      description = "Branch if equal : Branch to statement at label's " +
        "address if \$t1 and \$t2 are equal",
      format = Format.I,
      pattern = INAME_RS_RT_OFFSET)

    @JvmField val BNE = Instruction(
      iname = "bne",
      opcode = 5, //0x05
      mnemonicRepresentation = "bne \$t1, \$t2, 4",
      numericRepresentation = 0x152A0004,
      description = "Branch if not equal : Branch to statement at label's " +
        "address if \$t1 and \$t2 are not equal",
      format = Format.I,
      pattern = INAME_RS_RT_OFFSET)

    @JvmField val BLEZ = Instruction(
      iname = "blez",
      opcode = 6, //0x06
      mnemonicRepresentation = "blez \$t1, 4",
      numericRepresentation = 0x19200004,
      description = "Branch if less than or equal to zero : Branch to " +
        "statement at label's address if \$t1 is less than or equal to zero",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BGTZ = Instruction(
      iname = "bgtz",
      opcode = 7, //0x07
      mnemonicRepresentation = "bgtz \$t1, 4",
      numericRepresentation = 0x1D200004,
      description = "Branch if greater than zero : Branch to statement at " +
        "label's address if \$t1 is greater than zero",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val ADDI = Instruction(
      iname = "addi",
      opcode = 8, //0x08
      mnemonicRepresentation = "addi \$t1, \$10, 4",
      numericRepresentation = 0x21490004,
      description = "Addition immediate with overflow : set \$t1 to (\$10 " +
        "plus signed 16-bit immediate)",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val ADDIU = Instruction(
      iname = "addiu",
      opcode = 9, //0x09
      mnemonicRepresentation = "addiu \$t1, \$t2, 4",
      numericRepresentation = 0x25490004,
      description = "Addition immediate with overflow : set \$t1 to (\$t2 " +
        "plus signed 16-bit immediate)",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val SLTI = Instruction(
      iname = "slti",
      opcode = 10, //0x0a
      mnemonicRepresentation = "slti \$t1, \$t2, 4",
      numericRepresentation = 0x29490004,
      description = "Set less than immediate : If \$t2 is less than " +
        "sign-extended 16-bit immediate, then set \$t1 to 1 else set \$t1 to 0",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val SLTIU = Instruction(
      iname = "sltiu",
      opcode = 11, //0x0b
      mnemonicRepresentation = "sltiu \$t1, \$t2, 4",
      numericRepresentation = 0x2D490004,
      description = "Set less than immediate unsigned : If \$t2 is less than" +
        " sign-extended 16-bit immediate using unsigned comparison, " +
        "then set \$t1 to 1 else set \$t1 to 0",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val ANDI = Instruction(
      iname = "andi",
      opcode = 12, //0x0c
      mnemonicRepresentation = "andi \$t1, \$t2, 4",
      numericRepresentation = 0x31490004,
      description = "Bitwise AND immediate : Set \$t1 to bitwise AND of " +
        "\$t2 and zero-extended 16-bit immediate",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val ORI = Instruction(
      iname = "ori",
      opcode = 13, //0x0d
      mnemonicRepresentation = "ori \$t1, \$t2, 4",
      numericRepresentation = 0x35490004,
      description = "Bitwise OR immediate : Set \$t1 to bitwise OR of \$t2 " +
        "and zero-extended 16-bit immediate",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val XORI = Instruction(
      iname = "xori",
      opcode = 14, //0x0e
      mnemonicRepresentation = "xori \$t1, \$t2, 4",
      numericRepresentation = 0x39490004,
      description = "Bitwise XOR immediate : Set \$t1 to bitwise XOR " +
        "of \$t2 and zero-extended 16-bit immediate",
      format = Format.I,
      pattern = INAME_RT_RS_OFFSET)

    @JvmField val LUI = Instruction(
      iname = "lui",
      opcode = 15, //0x0f
      mnemonicRepresentation = "lui \$t1, 4",
      numericRepresentation = 0x3C090004,
      description = "Load upper immediate : Set high-order 16 bits of \$t1 " +
        "to 16-bit immediate and low-order 16 bits to 0",
      format = Format.I,
      pattern = INAME_RT_OFFSET)

    //TODO: op code 16-18

    @JvmField val BEQL = Instruction(
      iname = "beql",
      opcode = 20,
      mnemonicRepresentation = "beql \$t1, \$t2, 6",
      numericRepresentation = 0x512A0006,
      description = "Branch on Equal Likely: " +
        "if (rs = rt) then branch_likely. An 18-bit signed " +
        "offset (the 16-bit offset field shifted " +
        "left 2 bits) is added to the address of the instruction following " +
        "the branch (not the branch itself), in the branch delay slot, to " +
        "form a PC-relative effective target address. If the contents of " +
        "GPR rs and GPR rt are equal, branch to the target address after " +
        "the instruction in the delay slot is executed. If the branch is " +
        "not taken, the instruction in the delay slot is not executed.",
      format = Format.I,
      pattern = INAME_RS_RT_OFFSET)

    @JvmField val BNEL = Instruction(
      iname = "bnel",
      opcode = 21,
      mnemonicRepresentation = "bnel \$t1, \$t2, 6",
      numericRepresentation = 0x552A0006,
      description = "Branch on Not Equal Likely:" +
        "if (rs != rt) then branch_likely. An 18-bit signed " +
        "offset (the 16-bit offset field shifted left 2 bits) is added to " +
        "the address of the instruction following the branch (not the " +
        "branch itself), in the branch delay slot, to form a PC-relative " +
        "effective target address.If the contents of GPR rs and GPR rt are " +
        "not equal, branch to the effective target address after the " +
        "instruction in the delay slot is executed. If the branch is not " +
        "taken, the instruction in the delay slot is not executed.",
      format = Format.I,
      pattern = INAME_RS_RT_OFFSET)

    @JvmField val BLEZL = Instruction(
      iname = "blezl",
      opcode = 22,
      mnemonicRepresentation = "blezl \$t1, 6",
      numericRepresentation = 0x59200006,
      description = "Branch on Less Than or Equal to Zero Likely: " +
        "if (rs <= 0) then branch_likely. An 18-bit signed " +
        "offset (the 16-bit offset field shifted left 2 bits) is added " +
        "to the address of the instruction following the branch (not the " +
        "branch itself), in the branch delay slot, to form a " +
        "PC-relative effective target address. If the contents of GPR rs " +
        "are less than or equal to zero (sign bit is 1 or value is zero), " +
        "branch to the effective target address after the instruction in the " +
        "delay slot is executed. If the branch is not taken, the instruction " +
        "in the delay slot is not executed.",
      format = Format.I,
      pattern = INAME_RS_OFFSET)

    @JvmField val BGTZL = Instruction(
      iname = "bgtzl",
      opcode = 23,
      mnemonicRepresentation = "bgtzl \$t1, 6",
      numericRepresentation = 0x5D200006,
      description = "Branch on Greater Than Zero Likely: " +
        "if (rs > 0) then branch_likely. An 18-bit signed offset (the " +
        "16-bit offset field shifted left 2 bits) is added to the address " +
        "of the instruction following the branch (not the branch itself), " +
        "in the branch delay slot, to form a PC-relative effective " +
        "target address. If the contents of GPR rs are greater than zero " +
        "(sign bit is 0 but value not zero), branch to the effective target " +
        "address after the instruction in the delay slot is executed. If " +
        "the branch is not taken, the instruction in the delay slot is " +
        "not executed.",
      format = Format.I,
      pattern = INAME_RS_OFFSET)


    /* OP-code: 28(_10) 0x1c(_16) 011100(_2) */
    @JvmField val MADD = Instruction(
      iname = "madd",
      opcode = 28,
      funct = 0,
      mnemonicRepresentation = "madd \$t1, \$t2",
      numericRepresentation = 0x712A0000,
      description = "Multiply add : Multiply \$t1 by \$t2 then " +
        "increment HI by high-order 32 bits of product, " +
        "increment LO by low-order 32 bits of product (use mfhi " +
        "to access HI, mflo to access LO)",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val MADDU = Instruction(
      iname = "maddu",
      opcode = 28,
      funct = 1,
      mnemonicRepresentation = "maddu \$t1, \$t2",
      numericRepresentation = 0x712A0001,
      description = "Multiply add unsigned : Multiply \$t1 by \$t2 " +
        "then increment HI by high-order 32 bits of product, " +
        "increment LO by low-order 32 bits of product, " +
        "unsigned (use mfhi to access HI, mflo to access LO",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val MUL = Instruction(
      iname = "mul",
      opcode = 28,
      funct = 2,
      mnemonicRepresentation = "mul \$v0, \$a0, \$v0",
      numericRepresentation = 0x70821002,
      description = "Multiplication without overflow  : Set HI to " +
        "high-order 32 bits, LO and \$t1 to low-order 32 bits of " +
        "the product of \$t2 and \$t3 (use mfhi to access HI, mflo " +
        "to access LO)",
      format = Format.R,
      pattern = INAME_RD_RS_RT)

    @JvmField val MSUB = Instruction(
      iname = "msub",
      opcode = 28, //0x1c,
      funct = 4,
      mnemonicRepresentation = "msub \$t1, \$t2",
      numericRepresentation = 0x712A0004,
      description = "Multiply subtract : Multiply \$t1 by \$t2 then " +
        "decrement HI by high-order 32 bits of product, decrement LO by " +
        "low-order 32 bits of product (use mfhi to access HI, " +
        "mflo to access LO)",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val MSUBU = Instruction(
      iname = "msubu",
      opcode = 28, //0x1c,
      funct = 5,
      mnemonicRepresentation = "msubu \$t1, \$t2",
      numericRepresentation = 0x712A0005,
      description = "Multiply subtract unsigned : Multiply \$t1 by \$t2" +
              " then decrement HI by high-order 32 bits of product, " +
              "decement LO by low-order 32 bits of product, unsigned " +
              "(use mfhi to access HI, mflo to access LO)",
      format = Format.R,
      pattern = INAME_RS_RT)

    @JvmField val CLZ = Instruction(
      iname = "clz",
      opcode = 28, //0x20,
      funct = 32,
      mnemonicRepresentation = "clz \$t1, \$t2",
      numericRepresentation = 0x71404820,
      description = "Count number of leading zeroes : Set \$t1 to the count " +
        "of leading zero bits in \$t2 starting at most significant bit position.",
      format = Format.R,
      pattern = INAME_RD_RS)

    @JvmField val CLO = Instruction(
      iname = "clo",
      opcode = 28, //0x20,
      funct = 33,
      mnemonicRepresentation = "clo \$t1, \$t2",
      numericRepresentation = 0x71404821,
      description = "Count number of leading ones : Set \$t1 to the count of " +
        "leading one bits in \$t2 starting at most significant bit position.",
      format = Format.R,
      pattern = INAME_RD_RS)
    /* End op-code 28 */

    @JvmField val LB = Instruction(
      iname = "lb",
      opcode = 32,
      mnemonicRepresentation = "lb \$t1, 7(\$t2)",
      numericRepresentation = 0x81490007,
      description = "Load byte : Set \$t1 to sign-extended 8-bit value " +
        "from effective memory byte address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LH = Instruction(
      iname = "lh",
      opcode = 33,
      mnemonicRepresentation = "lh \$t1, 8(\$t2)",
      numericRepresentation = 0x85490008,
      description = "Load halfword : Set \$t1 to sign-extended 16-bit " +
        "value from effective memory halfword address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LWL = Instruction(
      iname = "lwl",
      opcode = 34,
      mnemonicRepresentation = "lwl \$t1, 9(\$t2)",
      numericRepresentation = 0x89490009,
      description = "Load word left : Load from 1 to 4 bytes left-justified " +
        "into \$t1, starting with effective memory byte address and " +
        "continuing through the low-order byte of its word",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LW = Instruction(
      iname = "lw",
      opcode = 35,
      mnemonicRepresentation = "lw \$t1, 10(\$t2)",
      numericRepresentation = 0x8D49000a,
      description = "Load word : Set \$t1 to contents of effective memory " +
        "word address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LBU = Instruction(
      iname = "lbu",
      opcode = 36,
      mnemonicRepresentation = "lbu \$t1, 11(\$t2)",
      numericRepresentation = 0x9149000B,
      description = "Load byte unsigned : Set \$t1 to zero-extended 8-bit " +
        "value from effective memory byte address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LHU = Instruction(
      iname = "lhu",
      opcode = 37,
      mnemonicRepresentation = "lhu \$t1, 12(\$t2)",
      numericRepresentation = 0x9549000C,
      description = "Load halfword unsigned : Set \$t1 to zero-extended " +
        "16-bit value from effective memory halfword address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LWR = Instruction(
      iname = "lwr",
      opcode = 38,
      mnemonicRepresentation = "lwr \$t1, 13(\$t2)",
      numericRepresentation = 0x9949000D,
      description = "Load word right : Load from 1 to 4 bytes right-justified" +
        " into \$t1, starting with effective memory byte address and " +
        "continuing through the high-order byte of its word",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SB = Instruction(
      iname = "sb",
      opcode = 40, // 0x28
      mnemonicRepresentation = "sb \$t1, 4(\$t2)",
      numericRepresentation = 0xA1490004,
      description = "Store byte : Store the low-order 8 bits of \$t1 " +
        "into the effective memory byte address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SH = Instruction(
      iname = "sh",
      opcode = 41, // 0x29
      mnemonicRepresentation = "sh \$t1, 4(\$t2)",
      numericRepresentation = 0xA5490004,
      description = "Store halfword : Store the low-order 16 bits of \$t1 " +
        "into the effective memory halfword address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SWL = Instruction(
      iname = "swl",
      opcode = 42, // 0x2a
      mnemonicRepresentation = "swl \$t1, 4(\$t2)",
      numericRepresentation = 0xA9490004,
      description = "Store word left : Store high-order 1 to 4 bytes of " +
        "\$t1 into memory, starting with effective byte address and " +
        "continuing through the low-order byte of its word",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SW = Instruction(
      iname = "sw",
      opcode = 43, // 0x2b
      mnemonicRepresentation = "sw \$ra, 4(\$sp)",
      numericRepresentation = 0xAFBF0004, //2948530180,
      description = "Store the word from register \$rt at offset.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    //TODO: cache

    @JvmField val LL = Instruction(
      iname = "ll",
      opcode = 48, // 0x30
      mnemonicRepresentation = "ll \$ra, 4(\$sp)",
      numericRepresentation = 0xC3BF0004,
      description = "Load linked : Paired with Store Conditional (sc) " +
        "to perform atomic read-modify-write.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LWC1 = Instruction(
      iname = "lwc1",
      opcode = 49, // 0x31
      mnemonicRepresentation = "lwc1 \$ra, 4(\$sp)",
      numericRepresentation = 0xC7BF0004,
      description = "Load word into Coprocessor 1 (FPU) : Set \$ra to 32-bit " +
        "value from effective memory word address.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LWC2 = Instruction(
      iname = "lwc2",
      opcode = 50, // 0x32
      mnemonicRepresentation = "lwc2 \$ra, 4(\$sp)",
      numericRepresentation = 0xCBBF0004,
      description = "Load word into Coprocessor 2 (FPU) : Set \$ra to 32-bit " +
        "value from effective memory word address.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val PREF = Instruction(
      iname = "pref",
      opcode = 51, // 0x33
      mnemonicRepresentation = "pref 1, 2(\$sp)",
      numericRepresentation = 0xCFA10002,
      description = "Load double word Coprocessor 1 (FPU)) : Set \$t1 to " +
        "64-bit value from effective memory doubleword address.",
      format = Format.I,
      pattern = INAME_HINT_ADDRESS)

    @JvmField val LDC1 = Instruction(
      iname = "ldc1",
      opcode = 53, // 0x35
      mnemonicRepresentation = "ldc1 \$t1, 4(\$sp)",
      numericRepresentation = 0xD7A90004,
      description = "Load double word Coprocessor 1 (FPU)) : Set \$t1 to " +
        "64-bit value from effective memory doubleword address.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val LDC2 = Instruction(
      iname = "ldc2",
      opcode = 54, // 0x36
      mnemonicRepresentation = "ldc2 \$t1, 4(\$sp)",
      numericRepresentation = 0xDBA90004,
      description = "Load double word Coprocessor 2 (FPU)) : Set \$t1 to " +
        "64-bit value from effective memory doubleword address.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SC = Instruction(
      iname = "sc",
      opcode = 56, // 0x38
      mnemonicRepresentation = "sc \$t1, 4(\$sp)",
      numericRepresentation = 0xE3A90004,
      description = "Store conditional : Paired with Load Linked (ll) to " +
        "perform atomic read-modify-write.  Stores \$t1 value into " +
        "effective address, then sets \$t1 to 1 for success.",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SWC1 = Instruction(
      iname = "swc1",
      opcode = 57, // 0x39
      mnemonicRepresentation = "swc1 \$t1, 4(\$sp)",
      numericRepresentation = 0xE7A90004,
      description = "Store word from Coprocesor 1 (FPU) : Store 32 bit value " +
        "in \$t1 to effective memory word address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SWC2 = Instruction(
      iname = "swc2",
      opcode = 58, // 0x3a
      mnemonicRepresentation = "swc2 \$t1, 4(\$sp)",
      numericRepresentation = 0xEBA90004,
      description = "Store word from Coprocesor 2 (FPU) : Store 32 bit value " +
        "in \$t1 to effective memory word address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SDC1 = Instruction(
      iname = "sdc1",
      opcode = 61, // 0x3d
      mnemonicRepresentation = "sdc1 \$t1, 4(\$sp)",
      numericRepresentation = 0xF7A90004,
      description = "Store double word from Coprocessor 1 (FPU)) : " +
        "Store 64 bit value in \$t1 to effective memory doubleword address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)

    @JvmField val SDC2 = Instruction(
      iname = "sdc2",
      opcode = 62, // 0x3e
      mnemonicRepresentation = "sdc2 \$t1, 4(\$sp)",
      numericRepresentation = 0xFBA90004,
      description = "Store double word from Coprocessor 2 (FPU)) : " +
        "Store 64 bit value in \$t1 to effective memory doubleword address",
      format = Format.I,
      pattern = INAME_RT_ADDRESS)


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
      val inst = from(Integer.toUnsignedLong(machineCode.toInt()))
      try {
        // Attempt to get the left projection and return it.
        return inst.left().get()
      } catch (e : NoSuchElementException) {
        throw NoSuchElementException("Attempted to get an instruction from $machineCode" +
          " but got " + inst.right().get())
      }
    }

    @Throws(NoSuchInstructionException::class)
    @JvmStatic fun from(machineCode: Long): Either<Instruction, PartiallyValidInstruction> {
      val opcode: Int = machineCode.opcode().toInt()
      if (opcode < 0 || opcode > 62) {
        throw NoSuchInstructionException(machineCode)
      }
      // Check if the entire number is 0s, then we have a nop instruction
      // Once again, nop and sll clashes on the (opcode, funct) tuple so
      // we have to treat one of them as a special-case. Nop seemed easiest
      // to handle as a special case.
      if (machineCode == Integer.toUnsignedLong(0)) {
        return Either.left(NOP)
      }

      val prototype: Instruction?
      if (opcode == 0) {
        prototype = opcodeEquals0x00IdentifiedByFunct[machineCode.funct()]
      } else if (opcode == 0x1c) {
        prototype = opcodeEquals0x1cIdentifiedByFunct[machineCode.funct()]
      } else if (opcode == 0x01) {
        prototype = opcodeEquals0x01IdentifiedByRt[machineCode.rt()]
      } else {
        prototype = identifiedByTheirOpcodeAlone[opcode]
      }

      if (prototype == null) {
        throw NoSuchInstructionException(machineCode)
      }

      return prototype.pattern.invoke(prototype, machineCode)
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

