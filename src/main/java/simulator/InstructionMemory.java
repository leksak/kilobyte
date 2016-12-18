package simulator;

import common.instruction.Instruction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Value
public class InstructionMemory {
  // Each instruction is 32 bits, or 4 bytes. An int is 32 bits.
  // We need to support a minimum of 1000 bytes of instruction memory.
  // Hence, we need to be able to store _at least_ 250 instructions.
  @Getter(AccessLevel.PRIVATE)
  int SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS;
  Instruction[] instructions;
  @NonFinal
  int index = 0;

  private InstructionMemory() {
    // Intentionally left empty
    this(1000);
  }

  private InstructionMemory(int numberOfBytes) {
    this.SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS = numberOfBytes / 4;
    instructions = new Instruction[SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS];
  }

  /**
   * Inits 1000 bytes of instruction memory
   *
   * @return a 1000-bytes of instruction memory
   */
  public static InstructionMemory init() {
    return init(1000);
  }

  public static InstructionMemory init(int numberOfBytes) {
    return new InstructionMemory(numberOfBytes);
  }

  public Instruction fromAddress(int address) {
    // The program counter expects the memory to be laid out in bytes,
    // hence it will (should) always be divisible by 4. So, if
    // the address is 0 then the first instruction should be fetched.
    // If the address is 4 then the second instruction should be fetched
    // since the first instruction takes up exactly 4 bytes of memory.
    checkArgument(address % 4 == 0,
          "Expected the given address to be divisible by 4. Got " + address);
    checkArgument(address / 4 < SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS, String.format("Address out of range. Expected %d" +
          " to be inside the range [0, %d)", address, SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS));
    return instructions[address / 4];
  }

  public Instruction read(PC programCounter) {
    return fromAddress(programCounter.getCurrentAddress());
  }

  public void add(Instruction i) {
    if (index >= 250) {
      throw new IllegalStateException("Ran out of InstructionMemory");
    }
    instructions[index++] = i;
  }

  public void addAll(List<Instruction> instructions) {
    instructions.forEach(this::add);
  }
}
