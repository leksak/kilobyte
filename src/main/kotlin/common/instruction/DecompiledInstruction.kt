package common.instruction

import common.instruction.extensions.opcode
import java.util.*

sealed class DecompiledInstruction {
  class Valid(val instruction: Instruction) : DecompiledInstruction()
  class PartiallyValid(val instruction: Instruction, val errors: List<String>) : DecompiledInstruction()
  class UnknownInstruction(val machineCode: Long) : DecompiledInstruction()

  override fun toString() : String {
    when (this) {
      is Valid -> return instruction.toString()
      is PartiallyValid -> {
        val sj = StringJoiner("\", \"", "[\"", "\"]")
        errors.map { sj.add(it) }
        return instruction.toString() + " error(s)=" + sj.toString()
      }
      is UnknownInstruction -> {
        val op = machineCode.opcode()
        return "There is no known instruction corresponding to: \"$machineCode\". opcode=\"$op\""
      }
    }
  }

  fun asInstruction() : Instruction {
    when (this) { is Valid -> return instruction }
    throw IllegalStateException("\"asInstruction\" called on a non-valid instruction")
  }

  fun isValid() : Boolean {
    when (this) { is Valid -> return true }
    return false
  }

  fun isUnknown() : Boolean {
    when (this) { is UnknownInstruction -> return true }
    return false
  }

  fun isPartiallyValid() : Boolean {
    when (this) { is PartiallyValid -> return true }
    return false
  }

  fun errors() : List<String> {
    when (this) { is PartiallyValid -> return errors }
    return emptyList()
  }

  companion object {
    @JvmStatic fun from(machineCode: Long): DecompiledInstruction = Instruction.decompile(machineCode)

    @JvmStatic fun printAllInstructions() {
      Instruction.primordialSet.forEach(::println)
    }
  }
}

