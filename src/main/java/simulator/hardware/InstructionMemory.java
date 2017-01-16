package simulator.hardware;

import common.instruction.Format;
import common.instruction.Instruction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import simulator.ui.utils.Radix;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;


@Value
@Log
public class InstructionMemory implements Memory {
  // Each instruction is 32 bits, or 4 bytes. An int is 32 bits.
  // We need to support a minimum of 1000 bytes of instruction memory.
  // Hence, we need to be able to store _at least_ 250 instructions.
  @Getter(AccessLevel.PRIVATE)
  int SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS;
  Instruction[] instructions;
  @NonFinal
  int index = 0;

  private InstructionMemory(int numberOfBytes) {
    this.SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS = numberOfBytes / 4;
    instructions = new Instruction[SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS];
    resetMemory();
  }

  public String[] toStringArray(Radix r) {
    checkArgument(r == Radix.HEX || r == Radix.DECIMAL,
          "Expected the supplied radix to be either HEX or DECIMAL");
    int rad = -1;
    if (r == Radix.HEX) rad = 16;
    if (r == Radix.DECIMAL) rad = 10;

    String[] arr = new String[instructions.length];
    for (int i = 0; i < arr.length; i++) {
      Instruction inst = instructions[i];
      long numeric = inst.getNumericRepresentation();
      Format format = inst.getFormat();

      String asMachineCode;
      String decomposed;

      if (r == Radix.HEX) {
        decomposed = inst.asDecomposedHexadecimalString();
        asMachineCode = "0x" + Long.toString(numeric, rad);
      } else {
        decomposed = inst.asDecomposedDecimalString();
        asMachineCode = Long.toString(numeric);
      }

      arr[i] = String.format("%s: %s %s %s",
            inst.getIname(),
            asMachineCode,
            format,
            decomposed);
    }

    return arr;
  }

  @Override
  public void resetMemory() {
    // The memory should be set to zero initially
    for (int i = 0; i < SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS; i++) {
      instructions[i] = Instruction.NOP.deepCopy();
    }
    index = 0;
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

  private Instruction fromAddressGivenInBytes(int address) {
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
    log.info("Reading from PC.addressPointer="+programCounter.getAddressPointer());
    return fromAddressGivenInBytes(programCounter.getAddressPointer());
  }

  public Instruction getInstructionAt(int addressInNumberOfBytes) {
    return instructions[addressInNumberOfBytes /4];
  }

  /* Add a single instruction to memory */
  private void add(Instruction i) {
    log.info("Adding instruction={" + i + "} to memory");
    if (index >= SIZE_IN_TOTAL_NUMBER_OF_INSTRUCTIONS) {
      throw new IllegalStateException("Ran out of instruction memory");
    }
    instructions[index++] = i;
  }

  public void addAll(List<Instruction> instructions) {
    instructions.forEach(this::add);
  }
}
