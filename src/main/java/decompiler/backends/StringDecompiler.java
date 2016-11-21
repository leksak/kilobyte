package decompiler.backends;

import common.instruction.Instruction;
import common.instruction.PartiallyValidInstruction;
import io.atlassian.fugue.Either;

import java.util.Collection;

public class StringDecompiler implements DecompilerBackend<String> {
  @Override
  public Collection<Either<Instruction, PartiallyValidInstruction>> decompile(String source) {
    return null;
  }
}
