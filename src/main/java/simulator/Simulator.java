package simulator;

import com.google.common.collect.ImmutableSet;
import common.hardware.Register;
import common.hardware.RegisterFile;
import common.instruction.Format;
import common.instruction.Instruction;
import common.machinecode.OperationsKt;
import lombok.Getter;
import lombok.Value;
import simulator.ALUOperation.Operation;
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

  ALUOperation aluOperation = new ALUOperation();
  ALUArithmetic aluArithmetic = new ALUArithmetic(dataMemory);

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

  public int getRegisterValue(String mnemonic) {
    return registerFile.get(mnemonic).getValue();
  }


  public void setRegisterValue(String mnemonic, int value) {
    registerFile.get(mnemonic).setValue(value);
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


    switch(i.getFormat()) {
      case I:
        executeFormatI(i);
        break;
      case J:
        //executeFormatJ(i);
        break;
      case R:
        executeFormatR(i);
        break;
      case EXIT:
        // Exit
        break;

    }

  }

  private void executeFormatI(Instruction i) {
    /* 2. Two registers, $t1 and $t2 , are read from the register file. */
      // Instruction 25:21 read register 1 (rs)
      Register r1 = registerFile.get(OperationsKt.rs(i.getNumericRepresentation()));
      // Instruction 20:16 read register 2 (rt) + MUX1
      Register r2 = registerFile.get(OperationsKt.rt(i.getNumericRepresentation()));

    /*3.The ALU performs a subtract on the data values read from the register
       file. The value of PC + 4 is added to the sign-extended, lower 16 bits of
       the instruction ( offset ) shifted left by two; the result is the branch target
       address.
     */
    Operation aluArtOp;
    int weReallyDoNotCare = 0;
    aluArtOp = aluOperation.functionCode(aluControl.getAluOp0(),
          aluControl.getAluOp1(),
          weReallyDoNotCare);

    /* 3.1 The ALU performs a subtract on the data values read from the register file */
    int aluReturnCalc = aluArithmetic.Arithmetic(aluArtOp, r1, r2);

    /* The value of PC + 4 is added to the sign-extended, lower 16 bits of the
     * instruction ( offset ) shifted left by two; the result is the branch target address. */
    int ret15to0 = OperationsKt.offset(i.getNumericRepresentation());
    int signExtend = SignExtender.extend(ret15to0+programCounter.getCurrentAddress()+4);


    /* 4.	The Zero result from the ALU is used to decide which adder result to store
          into the PC.
    */
    //TODO: this


  }

  private void executeFormatR(Instruction i) {
    // Instruction 25:21 read register 1 (rs)
    Register r1 = registerFile.get(OperationsKt.rs(i.getNumericRepresentation()));
    // Instruction 20:16 read register 2 (rt) + MUX1
    Register r2 = registerFile.get(OperationsKt.rt(i.getNumericRepresentation()));

    // Instruction 15:0 sig-extend 16 -> 32 OR Instruction 5-0->ALU control


    //int ret15to0 = OperationsKt.bits(i.getNumericRepresentation(), 15,0);
    int ret5to0 = OperationsKt.bits(i.getNumericRepresentation(), 5,0);



    // ALU Control get ALU-Operation for arithmetic.
    Operation aluArtOp;
    aluArtOp = aluOperation.functionCode(aluControl.getAluOp0(),
                                         aluControl.getAluOp1(),
                                         ret5to0);

    // Execute ALU-Arithmetic and return output.
    int artCalc = aluArithmetic.Arithmetic(aluArtOp, r1, r2);

    //If ALUC-RegDst save to register then save in register.
    if (aluControl.getRegDst()) {
      Register writeRegister = registerFile.get(OperationsKt.rd(i.getNumericRepresentation()));
      writeRegister.setValue(artCalc);
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
