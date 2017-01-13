package simulator;

import common.hardware.Register;
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
  NOR(     "nor",              (a, b) -> ~(a | b),        1, 1, 0, 0);

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

  static ALUOperation from(boolean alu1, boolean alu0, int funct) {
    System.err.println("ali1:"+alu1+", alu0:"+alu0+" funct:"+Integer.toBinaryString(funct));
    ALUOperation op = null;
    if ((alu1 && !alu0) && funct == 0b100000) op = ADD;
    if ((alu1 && !alu0) && funct == 0b100010) op = SUBTRACT;
    if ((alu1 && !alu0) && funct == 0b100100) op = AND;
    if ((alu1 && !alu0) && funct == 0b100101) op = OR;
    if ((alu1 && !alu0) && funct == 0b101010) op = SLT;
    if ((alu1 && !alu0) && funct == 0b100111) op = NOR;

    if (!alu1 && alu0)  op = SUBTRACT;
    if (!alu1 && !alu0) op = ADD;

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
    return f.apply(a, b);
  }
}
