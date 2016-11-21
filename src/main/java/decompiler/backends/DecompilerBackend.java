package decompiler.backends;

import common.instruction.Instruction;
import common.instruction.PartiallyValidInstruction;
import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;

import java.io.IOException;
import java.util.Collection;

public interface DecompilerBackend<T> {
  Collection<Either<Instruction, PartiallyValidInstruction>> decompile(T source) throws IOException, NoSuchInstructionException;
}
