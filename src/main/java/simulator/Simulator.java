package simulator;

import com.google.common.collect.ImmutableSet;
import common.hardware.Field;
import common.hardware.Register;
import common.hardware.RegisterFile;
import common.instruction.Format;
import common.instruction.Instruction;
import lombok.Getter;
import lombok.Value;
import lombok.extern.java.Log;
import simulator.hardware.PC;
import simulator.program.Program;

import static com.google.common.base.Preconditions.checkArgument;
import static common.instruction.Instruction.*;
import static common.machinecode.OperationsKt.*;
import static java.lang.String.format;

@Value
@Log
public class Simulator {
  @Getter
  PC programCounter = new PC();
  @Getter
  RegisterFile registerFile = new RegisterFile();
  Control control = new Control();


  @Getter
  InstructionMemory instructionMemory = InstructionMemory.init();

  @Getter
  DataMemory dataMemory = new DataMemory();

  @Getter
  static ImmutableSet<Instruction> supportedInstructions = ImmutableSet.of(
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

  public void executeNextInstruction() {
    // Fetch the next instruction from memory.
    Instruction i = instructionMemory.read(programCounter);
    execute(i);
  }

  public int getRegisterValue(String mnemonic) {
    return registerFile.get(mnemonic).getValue();
  }


  public void setRegisterValue(String mnemonic, int value) {
    registerFile.get(mnemonic).setValue(value);
  }


  public void execute(Instruction i) {
    log.info("Executing " + i);

    // 1. The instruction is fetched, and the PC is incremented
    programCounter.stepForward();
    // Instruction 31:26 - AluController
    control.updateOperationType(i.getOpcode());


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
    int r1Value, r2Value;
    int ret15to0 = offset(i);
    int signExtend = SignExtender.extend(ret15to0);
    /* 2. Two registers, $t1 and $t2 , are read from the register file. */
      // Instruction 25:21 read register 1 (rs)
      Register r1 = registerFile.get(Field.RS, i);
      r1Value = r1.getValue();
      // Instruction 20:16 read register 2 (rt) + MUX1
      Register r2 = registerFile.get(Field.RT, i);
      r2Value = r2.getValue();
      if (control.getAluSrc()) {
       r2Value = signExtend;
      }

    /*3.The ALU performs a subtract on the data values read from the register
       file. The value of PC + 4 is added to the sign-extended, lower 16 bits of
       the instruction ( offset ) shifted left by two; the result is the branch target
       address.
     */

    /* 3.1 The ALU performs a subtract on the data values read from the register file */
    boolean alu1 = control.getAluOp1();
    boolean alu0 = control.getAluOp0();
    ALUOperation aluArtOp = ALUOperation.from(alu1, alu0, funct(i));
    int result = aluArtOp.apply(r1Value, r2Value);

    /* 3.2 The value of PC + 4 is added to the sign-extended, lower 16 bits of
     *     the instruction ( offset ) shifted left by two; the result is the
     *     branch target address. */
    signExtend = signExtend << 2;
    signExtend = signExtend | programCounter.getAddressPointer();
    /* 4.
     * The Zero result from the ALU is used to decide which adder result to
     * store into the PC.
     */
    if (result == 0 && control.getBranch()) {
      log.info("Branching to address=" + signExtend);
      //can be off by 4 since the programCounter av incremented already? 8 lines up.
      programCounter.setTo(signExtend);
    }
    if (control.getMemtoReg()) {
      //System.err.println("result"+result);
      r2.setValue(dataMemory.readWord(result));
    }
    if (control.getMemWrite() && control.getAluSrc()) {
      log.info(format("Writing word to Memory Address=%d Value=%d", result, r2.getValue()));
      dataMemory.setMemory(result, (byte)r2.getValue());
    }
  }

  private void executeFormatR(Instruction i) {
    checkArgument(i.getFormat() == Format.R);
    // Instruction 25:21 read register 1 (rs)
    Register r1 = registerFile.get(Field.RS, i);
    // Instruction 20:16 read register 2 (rt) + MUX1
    Register r2 = registerFile.get(Field.RT, i);

    // Instruction 15:0 sig-extend 16 -> 32 OR Instruction 5-0->ALU control


    int ret5to0 = funct(i);

    // ALU Control get ALU-Operation for arithmetic.
    boolean alu1 = control.getAluOp1();
    boolean alu0 = control.getAluOp0();
    ALUOperation aluArtOp = ALUOperation.from(alu1, alu0, funct(i));
    int result = aluArtOp.apply(r1.getValue(), r2.getValue());

    // If ALUC-RegDst save to register
    if (control.getRegDst()) {
      registerFile.writeToRegister(Field.RD, i, result);
    }

  }

  public void execute(String s) {
    // Executes a single instruction
    execute(Instruction.from(s));
  }

  public void loadProgram(Program p) {
    instructionMemory.addAll(p.getInstructions());
  }

  public void setDataMemoryAtAddress(int address, Byte value) {
    dataMemory.setMemory(address, value);
  }

  public int getDataMemory(int address) {
    return dataMemory.readWord(address);
  }

  public Instruction getCurrentInstruction() {
    return instructionMemory.getInstructionAt(programCounter.getAddressPointer());
  }
}
