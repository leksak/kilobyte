package decompiler.backends;

import common.instruction.Instruction;
import common.instruction.MachineCodeDecoder;
import common.instruction.PartiallyValidInstruction;
import common.instruction.exceptions.NoSuchInstructionException;
import io.atlassian.fugue.Either;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FileDecompiler implements DecompilerBackend<File> {
  private boolean isNotNull(Object o) { return !Objects.isNull(o); }

  @Override
  public Collection<Either<Instruction, PartiallyValidInstruction>> decompile(File source) throws IOException, NoSuchInstructionException {
    BufferedReader br = new BufferedReader(
          new FileReader(source));
    List<Either<Instruction, PartiallyValidInstruction>> instructions = new ArrayList<>();

    String l;
    while (isNotNull(l = br.readLine())) {
      if (l.isEmpty()) {
        continue; // Skip empty lines
      }

      // The file is expected to contain a number on each line.
      // TODO: Catch exception
      instructions.add(Instruction.from(MachineCodeDecoder.decode(l));
    }

    return instructions;
  }
}
