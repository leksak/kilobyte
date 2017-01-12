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


  public void execute(Instruction i) {
    // 1. The instruction is fetched, and the PC is incremented
    programCounter.stepForward();
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

  /* lw $t1, offset($t2) in a style similar to Figure 4.19. Figure 4.20 shows
   * the active functional units and asserted control lines for a load. We can
   * think of a load instruction as operating in five steps (similar to the
   * R-type executed in four):
   * 1. An instruction is fetched from the instruction memory, and the PC is incremented.
   * 2. A register ($t2) value is read from the register file.
   * 3. The ALU computes the sum of the value read from the register file and the
   *    sign-extended, lower 16 bits of the instruction (offset).
   * 4. The sum from the ALU is used as the address for the data memory.
   * 5. The data from the memory unit is written into the register file; the register
   *    destination is given by bits 20:16 of the instruction ($t1) .
   */

  // add $t1, $t2,    $t3   - R -
  // beq  rs, rt,     label - I - Conditionally branch the number of
  //                              instructions specified by the offset if
  //                              register rs equals rt.
  // lw   rt, address       - I - Load the 32-bit quantity (word) at address
  //                              into register rt.


  /* Finally, we can show the operation of the branch-on-equal instruction,
   * such as beq $t1,$t2,offset, in the same fashion. It operates much like an
   * R-format instruction, but the ALU output is used to determine whether the
   * PC is written with PC + 4 or the branch target address.
   * Figure 4.21 shows the four steps in execution:
   * 1. An instruction is fetched from the instruction memory, and the PC is
   *    incremented.
   * 2. Two registers, $t1 and $t2, are read from the register file.
   * 3. The ALU performs a subtract on the data values read from the register
   *    file. The value of PC + 4 is added to the sign-extended, lower 16 bits
   *    of the instruction (offset) shifted left by two; the result is the
   *    branch target address.
   * 4. The Zero result from the ALU is used to decide which adder result to
   *    store into the PC.
   *
   */

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

    /* 3.1 The ALU performs a subtract on the data values read from the register file */
    Operation aluArtOp;
    int weReallyDoNotCare = 0;
    aluArtOp = aluOperation.functionCode(aluControl.getAluOp0(),
                                         aluControl.getAluOp1(),
                                         weReallyDoNotCare);
    int aluReturnCalc = aluArithmetic.Arithmetic(aluArtOp, r1, r2);

    /* 3.2 The value of PC + 4 is added to the sign-extended, lower 16 bits of
     *     the instruction ( offset ) shifted left by two; the result is the
     *     branch target address. */
    int ret15to0 = OperationsKt.offset(i.getNumericRepresentation());
    int signExtend = SignExtender.extend(ret15to0);
    signExtend = signExtend << 2;
    signExtend = signExtend | programCounter.getCurrentAddress();
    /* 4.	The Zero result from the ALU is used to decide which adder result to
     *    store into the PC.
     */
    if (aluReturnCalc == 0 && aluControl.getBranch()) {
      System.err.println("aluRet" + aluReturnCalc);
      //can be off by 4 since the programCounter av incremented already? 8 lines up.
      programCounter.increment(signExtend);
    }


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
