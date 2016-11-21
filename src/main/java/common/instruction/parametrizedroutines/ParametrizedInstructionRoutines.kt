package common.instruction.parametrizedroutines

import com.google.common.base.Preconditions.checkArgument
import common.hardware.Register
import common.instruction.*
import common.instruction.decomposedrepresentation.DecomposedRepresentation
import io.atlassian.fugue.Either
import java.util.*

val fieldNameToIndexMap: HashMap<String, Int> = hashMapOf(
  Pair("rs", 1),
  Pair("rt", 2),
  Pair("rd", 3),
  Pair("shamt", 4),
  Pair("funct", 5),
  Pair("offset", 3),
  Pair("address", 3),
  Pair("target", 1),
  Pair("hint", 2)
)


val fieldNameToMethodCallMap: HashMap<String, (n: Long) -> Int> = hashMapOf(
  Pair("rs", Long::rs),
  Pair("rt", Long::rt),
  Pair("rd", Long::rd),
  Pair("shamt", Long::shamt),
  Pair("funct", Long::funct),
  Pair("offset", Long::offset),
  Pair("target", Long::target),
  Pair("address", Long::offset),
  Pair("hint", Long::hint)
)


/** Hint-variable descriptions
 * source: http://www.cs.cmu.edu/afs/cs/academic/class/15740-f97/public/doc/mips-isa.pdf
 * page A-117
 * Reference: MIPS-instruction with prefix 'PREF'
 **/
enum class Hint (val value: Int) {
  LOAD(0),
  STORE(1),
  LOAD_STREAMED(3),
  STORE_STREAMED(5),
  LOAD_RETAINED(6),
  STORE_RETAINED(7);

  companion object {
    fun from(findValue: Int): Hint = Hint.values().first { it.value == findValue }
  }

  fun description(value: Int): String {
    when(value) {
      LOAD.value->return "Data is expected to be loaded (not modified). " +
        "Fetch data as if for a load"

      STORE.value->return "Data is expected to be stored or modified. " +
        "Fetch data as if for a store."
      LOAD_STREAMED.value->return "Data is expected to be loaded (not " +
        "modified) but not reused extensively; " +
        "it will “stream” through cache. Fetch " +
        "data as if for a load and place it in " +
        "the cache so that it will not displace " +
        "data prefetched as 'retained'."
      STORE_STREAMED.value->return "Data is expected to be stored or modified " +
        "but not reused extensively; it will " +
        "'stream' through cache. Fetch data as if " +
        "for a store and place it in the cache so " +
        "that it will not displace data " +
        "prefetched as 'retained'."
      LOAD_RETAINED.value->return "Data is expected to be loaded (not " +
        "modified) and reused extensively; it " +
        "should be “retained” in the cache. Fetch " +
        "data as if for a load and place it in the " +
        "cache so that it will not be displaced by " +
        "data prefetched as “streamed”"
      STORE_RETAINED.value->return "Data is expected to be stored or " +
        "modified and reused extensively; it " +
        "should be “retained” in the cache. " +
        "Fetch data as if for a store and place " +
        "it in the cache so that will not be " +
        "displaced by data prefetched as “streamed”."
      else->return "Not yet defined."
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
 * "abstract" String representation of the "mnemonic pattern".
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
 * representation of the instruction.
 *
 * As stated earlier, the opcode and funct field are accessible as
 * delegates supplied by another object and so the entire 32-bit number
 * can be evaluated.
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
fun from(format: Format, pattern: String): ParametrizedInstructionRoutine {
  /* We standardize the pattern to ensure consistency not out of necessity */
  val standardizedPattern = standardizeMnemonic(pattern)

  // Produces an array of the tokens in the pattern, for an example
  // we get that "iname rd, rs, rt".tokenize() yields ["rd", "rs", "rt"]
  // (note the boolean flag, which is why the "iname" disappears)
  val fields = standardizedPattern.tokenize(includeIname = false)

  fun shouldFieldBeZero(fieldName: String): Boolean {
    return !fields.contains(fieldName)
  }

  fun fieldIsNotZero(fieldName: String, machineCode: Long): Boolean {
    return fieldNameToMethodCallMap[fieldName]!!.invoke(machineCode) != 0
  }

  fun evaluateIfTheMnemonicRepresentationIsWellFormed(mnemonicRepresentation : String) {
    val expectedNumberOfCommas = standardizedPattern.countCommas()
    val expectedNumberOfArguments =
          standardizedPattern.replace(",", "").split(" ").size - 1
    throwIfIncorrectNumberOfCommas(expectedNumberOfCommas, mnemonicRepresentation)

    val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
    throwExceptionIfContainsIllegalCharacters(standardizedMnemonic)

    if (format == Format.I) {
      //throwExceptionIfNotContainsParentheses(standardizedMnemonic)
    } else /*if (format == Format.R || format == Format.J) */{
      // This pattern shouldn't contain any parens
      throwExceptionIfContainsParentheses(standardizedMnemonic)
    }

    throwIfIncorrectNumberOfArgs(expectedNumberOfArguments, standardizedMnemonic)
  }

  return object : ParametrizedInstructionRoutine {
    override fun invoke(prototype: Instruction,
                        mnemonicRepresentation: String): Instruction
    {
      evaluateIfTheMnemonicRepresentationIsWellFormed(mnemonicRepresentation)
      val standardizedMnemonic = standardizeMnemonic(mnemonicRepresentation)
      checkArgument(prototype.iname == standardizedMnemonic.iname())

      /* For instructions expressed using the pattern "iname rd, rs, rt"
       * we get that the contents of the tokens array will contain
       * the _values_ of rd, rs, and rt, like so:
       *
       * tokens=[rd, rs, rt]
       *
       * however, the arguments rd, rs, rt do not appear in the same
       * order as they have to when represented numerically so we
       * use the "fields" array which tells us what values we are observing
       * inside the tokens array together with "fieldNameToIndexMap"
       * to place the values at the correct places.
       */
      val tokens: Array<String> = standardizedMnemonic.tokenize(includeIname = false)
      val opcode = prototype.opcode

      // Equivalent in Java: new int[size]
      //
      // Will have as many cells as the instruction should be decomposed
      // into, so that for an R-format instruction the array is
      // 6 cells.
      val n = IntArray(format.noOfFields)

      n[0] = opcode

      if (format == Format.R || prototype.type == Type.J) {
        n[5] = prototype.funct!!
      }

      /* Will be set when op-code 1 */
      if (prototype.rt != null) {
        n[fieldNameToIndexMap.get("rt")!!] = prototype.rt!!
      }

      for (i in tokens.indices) {
        val destinationIndex: Int = fieldNameToIndexMap.get(fields[i])!!
        if (fields[i] == "offset" || fields[i] == "address") {
          n[destinationIndex] = Register.offsetFromOffset(tokens[i])
          if (fields[i] == "address") {
            n[fieldNameToIndexMap.get("rs")!!] = Register.registerFromOffset(tokens[i]).asInt()
          }
        }
        else if (fields[i] == "target") {
          n[destinationIndex] = Register.offsetFromOffset(tokens[i])
        }
        else if (fields[i] == "hint") {
          var hint = Register.offsetFromOffset(tokens[i])
          n[destinationIndex] = hint
          prototype.hint = Hint.from(hint)
        }
        else {
          n[destinationIndex] = Register.fromString(tokens[i]).asInt()
        }
      }

      val lengths = format.lengths
      val d = DecomposedRepresentation.fromIntArray(n, *lengths).asLong()
      return prototype(standardizedMnemonic, d)
    }

    override fun invoke(prototype: Instruction, machineCode: Long):
          Either<Instruction, PartiallyValidInstruction>
    {
      val iname = prototype.iname
        var mnemonicRepresentation = "$iname "
        for (i in fields.indices) {
          // The fields are given in order, so we can just concatenate
          // the strings.
          if (fields[i] == "rd") {
            mnemonicRepresentation += Register.fromInt(machineCode.rd()).toString()
          }

          if (fields[i] == "rt") {
            mnemonicRepresentation += Register.fromInt(machineCode.rt()).toString()
          }

          if (fields[i] == "rs") {
            mnemonicRepresentation += Register.fromInt(machineCode.rs()).toString()
          }

          if (fields[i] == "address") {
            mnemonicRepresentation += machineCode.offset().toString()
            if (!fields.contains("rs")  && iname != "lui") {
              mnemonicRepresentation += "("
              mnemonicRepresentation += Register.fromInt(machineCode.rs()).toString()
              mnemonicRepresentation += ")"
            }
          }

          if (fields[i] == "offset") {
            mnemonicRepresentation += machineCode.offset().toString()
          }

          if (fields[i] == "target") {
            mnemonicRepresentation += machineCode.target().toString()
          }

          if (fields[i] == "hint") {
            mnemonicRepresentation += machineCode.hint().toString()
            prototype.hint = Hint.from(machineCode.hint())
          }

          if (i != fields.indices.last) {
            mnemonicRepresentation += ", "
          }
        }
      val errors = ArrayList<String>()

      // We might get a trailing space if there are no args so
        // we remove it.
        mnemonicRepresentation = mnemonicRepresentation.trim()
        val inst = prototype(mnemonicRepresentation, machineCode)
      when (format) {
        Format.R -> {
          if (shouldFieldBeZero("shamt") && fieldIsNotZero("shamt", machineCode)) {
            errors.add("Expected shamt to be zero. Got ${machineCode.shamt()}")
          }

          if (shouldFieldBeZero("rd") && fieldIsNotZero("rd", machineCode)) {
            errors.add("Expected rd to be zero. Got ${machineCode.rd()}")
          }
          if (shouldFieldBeZero("rt") && fieldIsNotZero("rt", machineCode)) {
            errors.add("Expected rt to be zero. Got ${machineCode.rt()}")
          }
          if (shouldFieldBeZero("rs") && fieldIsNotZero("rs", machineCode)) {
            errors.add("Expected rs to be zero. Got ${machineCode.rs()}")
          }

          if (errors.isNotEmpty()) {
            return Either.right(PartiallyValidInstruction(inst, errors))
          }

          // Create a new copy using these values

          return Either.left(inst)
        }
        Format.I-> {
          return Either.left(inst)
        }
        Format.J-> {
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
@JvmField val INAME_RS_RT = from(Format.R, "iname rs, rt")
@JvmField val INAME_RD_RS = from(Format.R, "iname rd, rs")
@JvmField val INAME_RS = from(Format.R, "iname rs")
@JvmField val INAME_RD = from(Format.R, "iname rd")

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