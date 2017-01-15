package simulator;

import common.hardware.Register;
import common.instruction.Format;
import common.instruction.decomposedrepresentation.DecomposedRepresentation;
import lombok.extern.java.Log;

import java.util.function.BiFunction;

import static java.lang.String.format;

@Log
public enum ALUOperation implements BiFunction<Integer, Integer, Integer> {
  ADD(     "add",              (a, b) -> a + b,           0, 0, 1, 0),
  SUBTRACT("subtract",         (a, b) -> a - b,           0, 1, 1, 0),
  AND(     "and",              (a, b) -> a & b,           0, 0, 0, 0),
  OR(      "or",               (a, b) -> a | b,           0, 0, 0, 1),
  SLT(     "set on less than", (a, b) -> (a < b ? 1 : 0), 0, 1, 1, 1),
  NOR(     "nor",              (a, b) -> ~(a | b),        1, 1, 0, 0),
  SRL(     "srl",              (a, b) -> (a >>> b),       0, 0, 0, 0),
  SRA(     "sra",              (a, b) -> (a >> b),        0, 0, 0, 0);

  final String desiredALUAction;
  private BiFunction<Integer, Integer, Integer> f;
  private final int[] bits;

  private static boolean[] boolArray(int... arr) {
    boolean[] booleans = new boolean[arr.length];
    for (int i = 0; i < arr.length; i++) {
      booleans[i] = arr[i] == 1;
    }
    return booleans;
  }

  ALUOperation(String desiredALUAction, BiFunction<Integer, Integer, Integer> f, int... bits) {
    this.desiredALUAction = desiredALUAction;
    this.f = f;
    this.bits = bits;
  }

  private static String reverse(String s) {
    return new StringBuilder(s).reverse().toString();
  }
  
  public static boolean match(int n, String mask) {
    String actual = reverse(DecomposedRepresentation.asBitPattern(n));
    mask = reverse(mask);
    for (int i = 0; i < mask.length(); i++) {
      char maskChar = mask.charAt(i);
      if (maskChar == 'X') continue;
      if (maskChar != actual.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  static boolean match(String ALUOpMask, String functMask, int ALUOp10, int funct) {
    return match(ALUOp10, ALUOpMask) && match(funct, functMask);
  }

  static ALUOperation from(boolean alu1, boolean alu0) {
    return from(ALUOp10(alu1, alu0));
  }

  static ALUOperation from(int ALUOp10) {
    if (match("00", "XXXXXX", ALUOp10, 0)) return ADD;
    if (match("01", "XXXXXX", ALUOp10, 0)) return SUBTRACT;
    if (match("10", "XXXXXX", ALUOp10, 0)) return OR;
    throw new IllegalStateException("Unsupported operation");
  }

  static ALUOperation from(int ALUOp10, int funct) {
    if (match("10", "100111", ALUOp10, funct)) return NOR;
    if (match("10", "000010", ALUOp10, funct)) return SRL;
    if (match("10", "000011", ALUOp10, funct)) return SRA;
    if (match("1X", "XX0010", ALUOp10, funct)) return SUBTRACT; // Subtract
    if (match("10", "XX0000", ALUOp10, funct)) return ADD; // CTRLLINES=0010=ADD
    if (match("10", "XX0100", ALUOp10, funct)) return AND;
    if (match("10", "XX0101", ALUOp10, funct)) return OR;
    if (match("1X", "XX1010", ALUOp10, funct)) return SLT;
    throw new IllegalStateException("Unsupported operation");
  }
  
  static int ALUOp10(boolean alu1, boolean alu0) {
    int res = 0;
    if (alu1) res |= 0b10;
    if (alu0) res |= 0b01;
    return res;
  }

  static ALUOperation from(boolean alu1, boolean alu0, int funct) {
    int ALUOp10 = ALUOp10(alu1, alu0);
    ALUOperation op = from(ALUOp10, funct);
    assert(op != null);

    String aluOp = aluOpToString(alu1, alu0);
    String bin = "0b" + Integer.toBinaryString(funct);
    String action = op.desiredALUAction;
    log.info(format(
          "Fetching ALUOperation={ALUOp=%s funct=(bin=%s, dec=%d). Desired ALU action=\"%s\"", aluOp, bin, funct, action));

    return op;
  }

  private static String aluOpToString(boolean alu1, boolean alu0) {
    return boolToString(alu1) + boolToString(alu0);
  }
  
  private static String boolToString(boolean b) {
    return b ? "1" : "0";
  }

  public Integer apply(Integer a, Integer b) {
    log.info(format("Applying ALUOperation=%s to operands (v1=%d, v2=%d)", this.name(), a, b));
    return f.apply(a, b);
  }
}
