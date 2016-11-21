package decompiler.frontends;

import common.instruction.Instruction;
import common.instruction.PartiallyValidInstruction;
import io.atlassian.fugue.Either;

import java.util.Collection;

public interface DecompilerFrontEnd {
  void display(Collection<Either<Instruction, PartiallyValidInstruction>> instructions);
  String usage();
  void run(String[] args);
}
