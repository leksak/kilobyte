package kilobyte.simulator.program;

import com.google.common.collect.ImmutableList;
import kilobyte.common.instruction.Instruction;
import lombok.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class Program {
  // A program is just an ordered collection of instructions
  ImmutableList<Instruction> instructions;

  private Program(List<Instruction> instructions) {
    this.instructions = ImmutableList.copyOf(instructions);
  }
  public static Program from(Instruction... instructions) {
    return from(Arrays.asList(instructions));
  }

  public static Program from(String... instructions) {
    return from(Arrays.stream(instructions).map(Instruction::from).collect(Collectors.toList()));
  }

  public static Program from(File f) throws IOException {
    return from(new FileReader(f));
  }

  public static Program from(Reader r) throws IOException {
    BufferedReader br = new BufferedReader(r);

    List<Instruction> instructions = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      line = line.trim(); // Indentation doesn't matter
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
