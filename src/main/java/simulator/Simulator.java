package simulator;

import com.google.common.collect.ImmutableSet;
import common.hardware.Register;
import common.hardware.RegisterFile;
import common.instruction.Instruction;
import common.machinecode.OperationsKt;
import decompiler.MachineCodeDecoder;
import lombok.Getter;
import lombok.Value;
import simulator.program.Program;

import static common.instruction.Instruction.*;

@Value
public class Simulator {
  @Getter
  PC programCounter = new PC();
  @Getter
  RegisterFile registerFile = new RegisterFile();
  ALUControl aluControl = new ALUControl();


  @Getter
  InstructionMemory instructionMemory = InstructionMemory.init();

  @Getter
  DataMemory dataMemory = new DataMemory();

  ALUOperation aluOperationRegData = new ALUOperation(dataMemory);

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

  public void next() {
    // Fetch the next instruction from memory.
    Instruction i = instructionMemory.read(programCounter);
  }

  // add $t1,$t2,$t3
  // 1. The instruction is fetched, and the PC is incremented.
  // 2. Two registers, $t2 and $t3 , are read from the register file;
  //    also, the main control unit computes the setting of the control lines during this step.
  // 3. The ALU operates on the data read from the register file, using the function code
  //    (bits 5:0, which is the funct field, of the instruction) to generate the ALU function.
  // 4. The result from the ALU is written into the register file using
  //    bits 15:11 of the instruction to select the destination register ( $t1 ).
  public void execute(Instruction i) {
    // PC increment

    // Instruction 31:26 - AluController
    aluControl.updateOperationType(i.getOpcode());
    // Instruction 25:21 read register 1 (rs)
    Register r1 = registerFile.get(OperationsKt.rs(i.getNumericRepresentation()));

    // Instruction 20:16 read register 2 (rt) + MUX1
    Register r2 = registerFile.get(OperationsKt.rt(i.getNumericRepresentation()));
    //MUX0
    if (r2.getValue() > 0 ) {

    } else {

    }

    // Instruction 15:0 sig-extend 16 -> 32 OR Instruction 5-0->ALU control
    i.getNumericRepresentation();
    long instrpart0to15 = 0;
    int ret15to0 = OperationsKt.bits(instrpart0to15, 15,0);
    int ret5to0 = OperationsKt.bits(instrpart0to15, 5,0);

    if (aluControl.getAluOp0() || aluControl.getAluOp1()) {
      aluOperationRegData.functionCode(ret5to0);
    }



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

  public void loadProgram(Program p) {
    instructionMemory.addAll(p.getInstructions());
  }
}
