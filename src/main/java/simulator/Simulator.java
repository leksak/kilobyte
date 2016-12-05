package simulator;

import com.google.common.collect.ImmutableSet;
import common.instruction.Instruction;
import lombok.Getter;
import lombok.Value;

import java.io.File;
import java.util.List;

import static common.instruction.Instruction.*;

@Value
public class Simulator {
  @Getter
  PC programCounter = new PC();

  @Getter
  InstructionMemory instructionMemory = new InstructionMemory();
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

  public Object next() {
    throw new UnsupportedOperationException();
  }

  public void loadProgram(File f) {

  }

  public void loadProgram(List<Instruction> instructions) {

  }

  public void execute(Instruction i) {

  }

  public void execute(String s) {

  }

  /**
   * Prints the names of all the instructions contained in this set.
   * Useful for technical documentation.
   */
  public void printSupportedInstructions() {
    supportedInstructions.forEach(System.out::println);
  }


}
