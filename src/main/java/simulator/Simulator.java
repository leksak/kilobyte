package simulator;

import com.google.common.collect.ImmutableSet;
import common.hardware.RegisterFile;
import common.instruction.Instruction;
import lombok.Getter;
import lombok.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static common.instruction.Instruction.*;

@Value
public class Simulator {
  @Getter
  PC programCounter = new PC();
  @Getter
  RegisterFile rf = new RegisterFile();

  @Getter
  InstructionMemory instructionMemory = InstructionMemory.init();
  ImmutableSet<Instruction> supportedInstructions = ImmutableSet.of(
        ADD,
        SUB,
        AND,
        OR,
        NOR,
        SLT,
        LW,
        SW,
        BEQ,
        ADDI,
        ORI,
        SRL,
        SRA,
        J,
        JR,
        NOP
  );

  public void loadProgram(String filename) throws IOException {
    loadProgram(new File(filename));
  }

  public void loadProgram(File f) throws IOException {
    loadProgram(new FileReader(f));
  }

  public void loadProgram(Reader r) throws IOException {
    BufferedReader br = new BufferedReader(r);

    List<Instruction> instructions = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      if (line.isEmpty()) {
        continue;
      }

      instructions.add(Instruction.from(line));
    }

    loadProgram(instructions);
  }

  public void loadProgram(List<Instruction> instructions) {
    instructionMemory.addAll(instructions);
  }

  public void next() {
    // Fetch the next instruction from memory.
    Instruction i = instructionMemory.read(programCounter);
  }

  public void execute(Instruction i) {

  }

  public void execute(String s) {
    // Executes a single instruction
    execute(Instruction.from(s));
  }

  /**
   * Prints the names of all the instructions contained in this set.
   * Useful for technical documentation.
   */
  public void printSupportedInstructions() {
    supportedInstructions.forEach(System.out::println);
  }
}
