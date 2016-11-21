package decompiler.backends;

import common.instruction.Instruction;
import common.instruction.PartiallyValidInstruction;
import io.atlassian.fugue.Either;

import java.io.BufferedReader;
import java.util.Collection;

public class BufferedReaderDecompiler implements DecompilerBackend<BufferedReader> {
  @Override
  public Collection<Either<Instruction, PartiallyValidInstruction>> decompile(BufferedReader source) {
    return null;
  }
}
