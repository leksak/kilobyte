package kilobyte.common.instruction.parametrizedroutines

import com.google.common.base.Preconditions.checkArgument
import kilobyte.common.extensions.*
import kilobyte.common.hardware.RegisterFile
import kilobyte.common.instruction.DecompiledInstruction
import kilobyte.common.instruction.Format
import kilobyte.common.instruction.Instruction
import kilobyte.common.instruction.decomposedrepresentation.DecomposedRepresentation
import kilobyte.common.instruction.exceptions.IllegalCharactersInMnemonicException
import kilobyte.common.instruction.exceptions.MalformedMnemonicException
import kilobyte.common.instruction.mnemonic.iname
import kilobyte.common.instruction.mnemonic.standardizeMnemonic
import kilobyte.common.instruction.mnemonic.throwExceptionIfContainsIllegalCharacters
import kilobyte.common.instruction.mnemonic.throwIfIncorrectNumberOfCommas
import kilobyte.common.machinecode.*
import kilobyte.decompiler.MachineCodeDecoder
import java.util.*

val fieldNameToIndexMap = mapOf(
      "rs" to 1,
      "rt" to 2,
      "rd" to 3,
      "shamt" to 4,
      "funct" to 5,
      "offset" to 3,
      "address" to 3,
      "target" to 1,
      "hint" to 2 // pseudo field
)

fun indexOf(fieldName: String): Int {
  return fieldNameToIndexMap[fieldName]!!
}

val fieldNameToMethodCallMap: HashMap<String, (n: MachineCode) -> Int> = hashMapOf(
      Pair("rs", MachineCode::rs),
      Pair("rt", MachineCode::rt),
      Pair("rd", MachineCode::rd),
      Pair("shamt", MachineCode::shamt),
      Pair("funct", MachineCode::funct),
      Pair("offset", MachineCode::offset),
      Pair("target", MachineCode::target),
      Pair("address", MachineCode::offset),
      Pair("hint", MachineCode::hint)
)

/**
 * Hint-variable descriptions
 * source: http://www.cs.cmu.edu/afs/cs/academic/class/15740-f97/public/doc/mips-isa.pdf
 * page A-117
 * Reference: MIPS-instruction with prefix 'PREF'
 */
enum class Hint(val value: Int) {
  LOAD(0),
  STORE(1),
  LOAD_STREAMED(3),
  STORE_STREAMED(5),
  LOAD_RETAINED(6),
  STORE_RETAINED(7);

  companion object {
    fun from(findValue: Int): Hint = values().first { it.value == findValue }
  }

  fun description(value: Int): String {
    when (value) {
      LOAD.value -> return "Data is expected to be loaded (not modified). " +
            "Fetch data as if for a load"

      STORE.value -> return "Data is expected to be stored or modified. " +
            "Fetch data as if for a store."
      LOAD_STREAMED.value -> return "Data is expected to be loaded (not " +
            "modified) but not reused extensively; " +
            "it will “stream” through cache. Fetch " +
            "data as if for a load and place it in " +
            "the cache so that it will not displace " +
            "data prefetched as 'retained'."
      STORE_STREAMED.value -> return "Data is expected to be stored or modified " +
            "but not reused extensively; it will " +
            "'stream' through cache. Fetch data as if " +
            "for a store and place it in the cache so " +
            "that it will not displace data " +
            "prefetched as 'retained'."
      LOAD_RETAINED.value -> return "Data is expected to be loaded (not " +
            "modified) and reused extensively; it " +
            "should be “retained” in the cache. Fetch " +
            "data as if for a load and place it in the " +
            "cache so that it will not be displaced by " +
            "data prefetched as “streamed”"
      STORE_RETAINED.value -> return "Data is expected to be stored or " +
            "modified and reused extensively; it " +
            "should be “retained” in the cache. " +
            "Fetch data as if for a store and place " +
            "it in the cache so that will not be " +
            "displaced by data prefetched as “streamed”."
      else -> return "Not yet defined."
    }
  }
}


/**
 * This method creates objects implementing the ParametrizedInstructionRoutine
 * by utilizing the given format to make assertions as to what manner 32-bit
 * integers are to be interpreted and the given string as a template to
 * pattern-match supplied mnemonics against.
 *
 * This method creates bi-directional parametrized routines for
 * Instruction instantiation using the format of the instruction and a
 * "abstract" String representation of the "mnemonic pattern", i.e.
 *
 * "iname rd, rs, rt" is the "abstract" "mnemonic pattern"
 *
 * This text will first provide one example of how to interpret two
 * R-format instruction and an I-format instruction to showcase the
 * similarities between how to represent them despite them having
 * different "mnemonic patterns" to serve as a background for the
 * abstraction for creating these patterns from String representations
 * alone.
 *
 * Example 1 (R-format):
 * =====================
 *
 * The "add" instruction is expressed on the form,
 *
 *     iname rd, rs, rt                                (1.1)
 *
 * which means that for
 *
 *     add $t1, $t2, $t3                               (1.2)
 *
 * we get that rd=$t1, rs=$t2, and rt=$t3.
 *
 * Using the String representation of (1.1) we can determine the number of
 * arguments. The correct number of commas can then be inferred to be the
 * number of arguments minus one, i.e. (argc - 1) where argc is the
 * argument count.
 *
 * From the Format we know the constituents of the Instruction when
 * represented in its numerical form, for the R-format especially we
 * have that it decomposes into the following fields,
 *
 * | 6 bits  | 5 bits | 5 bits | 5 bits | 5 bits | 6 bits  |
 * |:-------:|:------:|:------:|:------:|:------:|:-------:|
 * | op      | rs     | rt     | rd     | shamt  | funct   |
 *
 * Hence, when parsing (1.2) we assign the arguments into an integer
 * array with 6 elements (all defaulting to zero) like so,
 *
 *     arr = [op, $t2, $t3, $t1, 0, funct]
 *                               ^
 *                               |
 *                         default value
 *
 * Note that the actual values for the opcode and funct field are
 * retrieved elsewhere. Since the shamt parameter is not part of the
 * "mnemonic pattern" then we know it to be zero.
 *
 * Important:
 * ==========
 *
 * Derived from the mnemonic pattern we know what constraints need to be
 * placed on the respective fields, in this example we have that shamt
 * has to be 0, to wit we can evaluate the legality of a given numeric
 * representation of the instruction. It is because of this property we
 * present the two operations (instantiation from a string and a number)
 * as a cohesive unit.
 *
 * As stated earlier, the "opcode" and "funct" field are accessible as
 * delegates supplied by another object and with that the entire 32-bit number
 * can be decompiled.
 *
 * Example 2 (R-format):
 * =====================
 *
 * The "mult" instruction is expressed on the form,
 *
 *     mult rs, rt, for an example "mult $t1, $t2"
 *
 * As in "Example 1" the text alone allows us to infer the expected
 * argument count, which is two of course, and the appropriate number of
 * commas. Combined with the fact that the instruction is in the R-format
 * and the opcode and funct field is retrievable elsewhere we get that
 *
 *     arr = [op, $t1, $t2, 0, 0, funct]
 *                          ^  ^
 *                          |  |
 *                     default values
 *
 * Example 3 (I-format):
 * =====================
 *
 * All I-format instructions are decomposed into fields of the same
 * length.
 *
 * An I-type instruction is determined uniquely by its opcode field.
 *
 * The opcode is the leftmost 6-bits of the instruction when
 * represented as a 32-bit number.
 *
 * From the Format we know the constituents of the Instruction when
 * represented in its numerical form. Specifically for the I-format we
 * have that it decomposes into the following fields,
 *
 * | 6 bits  | 5 bits | 5 bits | 16 bits |
 * |:-------:|:------:|:------:|:-------:|
 * | op      | rs     | rt     | offset  |
 *
 * I-format instructions are traditionally written on the form,
 *
 *     iname rt, offset
 *
 * For an example,
 *
 *     lw $t0, 24($s2)
 *
 * which is represented numerically as
 *
 * | op      | rs     | rt     | offset  |
 * |:-------:|:------:|:------:|:-------:|
 * | 0x23    | 18     | 8      | 24_{10} |
 *
 * meaning that rs=$s2, rt=$t0, and the offset=24.
 */
interface ParametrizedInstructionRoutine {
  fun invoke(prototype: Instruction, machineCode: MachineCode): DecompiledInstruction
  fun invoke(prototype: Instruction, mnemonicRepresentation: String): Instruction
}

fun from(format: Format, pattern: String): ParametrizedInstructionRoutine {
  /*
   * We standardize the pattern to ensure consistency not out of necessity.
   * That way we do not have to worry about fudging up our definitions
   * by writing say "iname rt,offset" instead of "iname rt, offset".
   */
  val standardizedPattern = standardizeMnemonic(pattern)

  /*
   * We create an array of the tokens in the standardized array, in the
   * above example we get that fields = ["iname", "rt", "offset"]
   */
  val fields = standardizedPattern.tokenize()

  return object : ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction,
                        mnemonicRepresentation: String): Instruction {
      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)
      val expectedNumberOfCommas = standardizedPattern.countCommas()
      throwIfIncorrectNumberOfCommas(expectedNumberOfCommas, mnemonicRepresentation)
      val expectedNumberOfArguments = standardizedPattern.replace(",", "").split(" ").size - 1
      throwIfIncorrectNumberOfArgs(expectedNumberOfArguments, standardizedMnemonic)
      if (!isAllowedToContainParentheses(format) && standardizedMnemonic.containsParentheses()) {
        throw IllegalCharactersInMnemonicException(standardizedMnemonic, "<parentheses>")
      }
      checkArgument(prototype.iname == standardizedMnemonic.iname())

      /*
       * For instructions expressed using the mnemonic-pattern "iname rd, rs, rt"
       * we get that the contents of the tokens array will contain
       * the _values_ of rd, rs, and rt, (and _not_ the string literals
       * "rd", "rs, "rt") like so:
       *
       *       tokens=[rd, rs, rt]
       *
       * however, the arguments rd, rs, rt do not appear in the same
       * order as they have to when represented numerically so we
       * use the "fields" array which tells us what values we are observing
       * inside the "tokens" array together with "fieldNameToIndexMap"
       * to place the values at the correct places.
       */
      val tokens: Array<String> = standardizedMnemonic.tokenize()
      val opcode = prototype.opcode
      val n = IntArray(format.noOfFields)
      n[0] = opcode
      if (format == Format.R || prototype == Instruction.JALR) {
        n[5] = prototype.funct!!
      }

      if (prototype.opcode == 1) {
        assert(prototype.rt != null)
        /* The prototype will have its rt field set whenever
         * its opcode is equal to 1. This is the case for
         * branch-instructions such as BLTZL, BGEZ but also
         * trap instructions!
         *
         * For instance, BLTZL: op=1, rt=2
         *              BGEZ:   op=1, rt=1
         *              TGEI:   op=1, rt=8
         */
        val index = indexOf("rt")
        n[index] = prototype.rt!!
      }

      // TODO: Why? return value is not captured does this happen in-place?
      formatMnemonic(tokens, n, prototype, fields)

      if (prototype == Instruction.JAL) {
        // The jump instruction (jal) specifies an absolute memory address
        // (in bytes) to jump to, but is coded without its last two bits.
        n[1] = (MachineCodeDecoder.decode(tokens[1]) shr 2).toInt()
      }

      val d = DecomposedRepresentation.fromIntArray(n, *format.lengths).asLong()
      return prototype(standardizedMnemonic, d)
    }


    /**
     * When in machineCode, we trust.
     **/
    override fun invoke(prototype: Instruction, machineCode: MachineCode): DecompiledInstruction {
      val mnemonicRepresentation = formatMachineCodeToMnemonic(prototype,
            machineCode,
            fields)
      val inst = prototype(mnemonicRepresentation, machineCode)
      val errors = errorCheckPrototype(machineCode, format, fields)
      if (errors.isNotEmpty()) {
        return DecompiledInstruction.PartiallyValid(inst, errors)
      }
      return DecompiledInstruction.Valid(inst)
    }
  }

}

private fun shouldFieldBeZero(fieldName: String, fields: Array<String>): Boolean {
  return !fields.contains(fieldName)
}

private fun fieldIsNotZero(fieldName: String, machineCode: MachineCode): Boolean {
  return fieldNameToMethodCallMap[fieldName]!!.invoke(machineCode) != 0
}

private fun errorCheckPrototype(machineCode: MachineCode,
                                format: Format,
                                fields: Array<String>): ArrayList<String> {
  val errors = ArrayList<String>()
  when (format) {
    Format.R -> {
      // TODO: Can definitely be refactored
      if (shouldFieldBeZero("shamt", fields) && fieldIsNotZero("shamt", machineCode)) {
        errors.add("Expected shamt to be zero. Got ${machineCode.shamt()}")
      }
      if (shouldFieldBeZero("rd", fields) && fieldIsNotZero("rd", machineCode)) {
        errors.add("Expected rd to be zero. Got ${machineCode.rd()}")
      }
      if (shouldFieldBeZero("rt", fields) && fieldIsNotZero("rt", machineCode)) {
        errors.add("Expected rt to be zero. Got ${machineCode.rt()}")
      }
      if (shouldFieldBeZero("rs", fields) && fieldIsNotZero("rs", machineCode)) {
        errors.add("Expected rs to be zero. Got ${machineCode.rs()}")
      }
    }
    Format.I -> {
    }
    Format.J -> {
    }
    else -> {
      throw IllegalStateException("Attempted to instantiate " +
            "a instruction from an unknown format. Format: $format")
    }
  }
  return errors
}

private fun formatMachineCodeToMnemonic(prototype: Instruction,
                                        machineCode: MachineCode,
                                        fields: Array<String>): String {
  val iname = prototype.iname
  var mnemonicRepresentation = "$iname "
  for (i in fields.indices) {
    // The fields are given in order, so we can just concatenate
    // the strings.
    when (fields[i]) {
      "rd" -> mnemonicRepresentation += RegisterFile.getMnemonic(machineCode.rd())
      "rt" -> mnemonicRepresentation += RegisterFile.getMnemonic(machineCode.rt())
      "rs" -> mnemonicRepresentation += RegisterFile.getMnemonic(machineCode.rs())
      "offset" -> mnemonicRepresentation += machineCode.offset().toString()
      "target" -> mnemonicRepresentation += machineCode.target().toString()
      "shamt" -> mnemonicRepresentation += machineCode.shamt().toString()
      "address" -> {
        mnemonicRepresentation += machineCode.offset().toString()
        if (!fields.contains("rs") && iname != "lui") {
          mnemonicRepresentation += "("
          mnemonicRepresentation += RegisterFile.getMnemonic(machineCode.rs())
          mnemonicRepresentation += ")"
        }
      }
      "hint" -> {
        mnemonicRepresentation += machineCode.hint().toString()
        prototype.hint = Hint.from(machineCode.hint())
      }
    }

    if (i != fields.indices.first && i != fields.indices.last) {
      mnemonicRepresentation += ", "
    }
  }
  return mnemonicRepresentation.trim()
}

private fun formatMnemonic(tokens: Array<String>, n: IntArray, prototype: Instruction, fields: Array<String>): Array<String> {
  for (i in 1..tokens.size - 1) {
    // from 1 so that we can skip the iname
    val destinationIndex: Int = indexOf(fields[i])
    when (fields[i]) {
      "target" -> n[destinationIndex] = tokens[i].getOffset()
      "offset" -> n[destinationIndex] = tokens[i].getOffset()
      "address" -> {
        n[destinationIndex] = tokens[i].getOffset()
        n[indexOf("rs")] = RegisterFile.indexOf(tokens[i].getRegister())
      }
      "hint" -> {
        val hint = tokens[i].getOffset()
        n[destinationIndex] = hint
        prototype.hint = Hint.from(hint)
      }
      "shamt" -> {
        // Handles for instance the "19" in sll $s1, $t1, 19
        n[destinationIndex] = tokens[i].toInt()
      }
      else -> n[destinationIndex] = RegisterFile.indexOf(tokens[i])
    }
  }
  return tokens
}

fun isAllowedToContainParentheses(format: Format): Boolean {
  return (format == Format.I)
}

/**
 * Check if given String contains correct number of arguments. Arguments is
 * how many parameters an instruction has been given.
 * Example:
 * add $t1, $t2, $t3   (3)
 * jr $t1             (1)
 * break              (0)
 */
fun throwIfIncorrectNumberOfArgs(expectedArgc: Int, standardizedMnemonic: String) {
  // -1 for the instruction name
  val withoutCommas = standardizedMnemonic.removeCommas()
  val actualArgc = withoutCommas.split(" ").size - 1

  if (expectedArgc == actualArgc) {
    return
  }

  val err = "Wrong number of arguments. Expected $expectedArgc arguments. Got: $actualArgc"
  throw MalformedMnemonicException(standardizedMnemonic, err)
}

@JvmField val EXIT_PATTERN = from(Format.EXIT, "iname")
@JvmField val INAME = from(Format.R, "iname")
@JvmField val INAME_RS = from(Format.R, "iname rs")
@JvmField val INAME_RD = from(Format.R, "iname rd")
@JvmField val INAME_RS_RT = from(Format.R, "iname rs, rt")
@JvmField val INAME_RD_RS = from(Format.R, "iname rd, rs")
@JvmField val INAME_RD_RS_RT = from(Format.R, "iname rd, rs, rt")

@JvmField val INAME_RD_RT_SHAMT = from(Format.R, "iname rd, rt, shamt")


/**
 * The difference between offset and address is that address will accept an
 * syntax of N($REG) while offset will not. Offset can be referred as
 * immediate-instruction-type like N whereas N is an number.
 */
@JvmField val INAME_RT_OFFSET = from(Format.I, "iname rt, offset")
@JvmField val INAME_RS_OFFSET = from(Format.I, "iname rs, offset")
@JvmField val INAME_RS_RT_OFFSET = from(Format.I, "iname rs, rt, offset")
@JvmField val INAME_RT_RS_OFFSET = from(Format.I, "iname rt, rs, offset")
@JvmField val INAME_RT_ADDRESS = from(Format.I, "iname rt, address")
@JvmField val INAME_HINT_ADDRESS = from(Format.I, "iname hint, address")

@JvmField val INAME_TARGET = from(Format.J, "iname target")