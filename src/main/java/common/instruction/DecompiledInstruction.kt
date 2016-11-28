package common.instruction

import common.instruction.exceptions.NoSuchInstructionException

sealed class DecompiledInstruction {
  class Valid(val instruction: Instruction) : DecompiledInstruction()
  class PartiallyValid(val instruction: PartiallyValidInstruction) : DecompiledInstruction()
  class NoSuchInstruction() : DecompiledInstruction()

  companion object {
    @JvmStatic fun from(machineCode: Long): DecompiledInstruction {
      try {
        val inst = Instruction.from(machineCode)
        if (inst.isLeft) {
          return DecompiledInstruction.Valid(inst.left().get())
        } else {
          return DecompiledInstruction.PartiallyValid(inst.right().get())
        }
      } catch (e: NoSuchInstructionException) {
        return DecompiledInstruction.NoSuchInstruction()
      }
    }
  }
}

