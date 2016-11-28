package decompiler;

import common.instruction.DecompiledInstruction;

public interface Decompiler {
  default DecompiledInstruction decompile(String number) {
    return decompile(MachineCodeDecoder.decode(number));
  }

  default DecompiledInstruction decompile(Long number) {
    return DecompiledInstruction.from(number);
  }
}
