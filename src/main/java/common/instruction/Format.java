package common.instruction;

public enum Format {
  R(6, 5, 5, 5, 5, 6), // Use to compose decomposed representation
  I(6, 5, 5, 16),
  J(6, 26);

  public final int[] lengths;

  Format(int... lengths) {
    this.lengths = lengths;
  }

  public static long fieldsToMachineCode(int opcode, int rs, int rt, int rd, int shamt, int funct) {
    return opcode << 26
          | rs << 21
          | rt << 16
          | rd << 11
          | shamt << 6
          | funct;
  }
}
