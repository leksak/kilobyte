package simulator.program;

import com.google.common.collect.ImmutableList;
import common.instruction.Instruction;
import lombok.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Value
public class Program {
  // A program is just an ordered collection of instructions
  ImmutableList<Instruction> instructions;

  private Program(List<Instruction> instructions) {
    this.instructions = ImmutableList.copyOf(instructions);
  }

  public static Program from(String filename) throws IOException {
    return from(new File(filename));
  }

  public static Program from(File f) throws IOException {
    return from(new FileReader(f));
  }

  public static Program from(Reader r) throws IOException {
    BufferedReader br = new BufferedReader(r);

    List<Instruction> instructions = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      if (line.isEmpty()) {
        continue;
      }

      instructions.add(Instruction.from(line));
    }

    return from(instructions);
  }

  public static Program from(List<Instruction> instructions) {
    return new Program(instructions);
  }
}