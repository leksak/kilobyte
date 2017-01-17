package kilobyte.common.instruction

import kilobyte.common.machinecode.MachineCode
import kilobyte.common.machinecode.opcode
import java.util.*

sealed class DecompiledInstruction {
  class Valid(val instruction: Instruction) : DecompiledInstruction()
  class PartiallyValid(val instruction: Instruction, val errors: List<String>) : DecompiledInstruction()
  class UnknownInstruction(val machineCode: MachineCode) : DecompiledInstruction()

  override fun toString(): String {
    when (this) {
      is Valid -> return instruction.toString()
      is PartiallyValid -> {
        val sj = StringJoiner("\", \"", "[\"", "\"]")
        errors.map { sj.add(it) }
        return instruction.toString() + " error(s)=" + sj.toString()
      }
      is UnknownInstruction -> {
        val op = machineCode.opcode()
        val sj = StringJoiner(" ")
        sj.add("Unknown instruction: \"$machineCode\". opcode=\"$op\".")
        val eitherFormatOrString = Instruction.formatFrom(op)

        if (eitherFormatOrString.isLeft) {
          // We were able to infer a format so we can decompose the machineCode
          val actualFormat = eitherFormatOrString.left().get()
          sj.add("Format=\"${actualFormat.name}\".")
          sj.add("Decomposition=${actualFormat.decompose(machineCode)}")
        } else {
          sj.add(eitherFormatOrString.right().get())
        }

        return sj.toString()
      }
    }
  }

  fun asInstruction(): Instruction {
    when (this) { is Valid -> return instruction }
    println(this)
    throw IllegalStateException("\"asInstruction\" called on a non-valid instruction")
  }

  fun isValid(): Boolean {
    when (this) { is Valid -> return true
    }
    return false
  }

  fun isUnknown(): Boolean {
    when (this) { is UnknownInstruction -> return true
    }
    return false
  }

  fun isPartiallyValid(): Boolean {
    when (this) { is PartiallyValid -> return true
    }
    return false
  }

  fun errors(): List<String> {
    when (this) { is PartiallyValid -> return errors
    }
    return emptyList()
  }

  companion object {
    @JvmStatic fun from(machineCode: MachineCode): DecompiledInstruction = Instruction.decompile(machineCode)

    @JvmStatic fun printAllInstructions() {
      Instruction.primordialSet.forEach(::println)
    }
  }
}

