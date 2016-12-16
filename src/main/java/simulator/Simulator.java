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

  public Instruction next() {
    // Fetch the next instruction from memory.
    Instruction ret = instructionMemory.read(programCounter);
    programCounter.increment(4);
    return ret;
  }

  public void execute(Instruction i) {

  }

  /**
   * Prints the names of all the instructions contained in this set.
   * Useful for technical documentation.
   */
  public void printSupportedInstructions() {
    supportedInstructions.forEach(System.out::println);
  }
}
