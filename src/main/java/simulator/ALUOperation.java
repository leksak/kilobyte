package simulator;

import org.jetbrains.annotations.Nullable;

public class ALUOperation {
  enum Operation{
    AND(0), OR(1), ADD(2), SUBTRACT(6), SETONLESSTHAN(7), NOR(12);

    private final int value;
    Operation(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    @Nullable
    public static Operation get(int i) {
      for (Operation o: Operation.values()) {
        if (o.getValue() == i) {
          return o;
        }
      }
      return null;
    }

    public static Operation getFunction(boolean[] operationBit) {
      int val = 0;
      int i = 0;
      for (; i < operationBit.length; i++) {
        //System.err.println("getFunc iterator:"+i + " pow:"+Math.pow(2, i));
        if (operationBit[i]) {
          val += Math.pow(2, i);
          //System.err.println(" TRUE");
        }
      }
      //System.err.println("getFunc:"+val);
      return Operation.get(val);

    }
  }
  DataMemory dataMemory;

  public ALUOperation() {
    this.dataMemory = dataMemory;
  }


  public Operation functionCode(boolean alu0, boolean alu1, int ret5to0) {
    boolean[] bit = new boolean[6];
    boolean[] OperationBit = new boolean[4];
    String s = "";
    s += alu1 ? "1" : "0";
    s += alu0 ? "1 " : "0 ";

    for (int i = 5; i >= 0; i--) {
      bit[i] = (ret5to0 & (1 << i)) != 0;
      s += bit[i] ? "1" : "0";
    }
  s+= "("+ret5to0+")";
    System.err.println(s);
    /* alu0-alu1 f5-f4-f3-f-2-f1-f0 == Operationbits*/

    /* 00 XXXXX == 0010 */
    if (!alu1 && !alu0) {
      OperationBit[3] = false;
      OperationBit[2] = false;
      OperationBit[1] = true;
      OperationBit[0] = false;
    } else
    /* 01 XXXXX == 0110 */
    if (!alu1 && alu0) {
       OperationBit[3] = false;
       OperationBit[2] = true;
       OperationBit[1] = true;
       OperationBit[0] = false;
     }else
     /* 10 XX0000 = 0010 */
    if (alu1 && !alu0 && !bit[3] && !bit[2] && !bit[1] && !bit[0] ) {
      OperationBit[3] = false;
      OperationBit[2] = false;
      OperationBit[1] = true;
      OperationBit[0] = false;
    }else
     /* 1X XX0010 = 0110 */
     if (alu1 && !bit[3] && !bit[2] && bit[1] && !bit[0] ) {
       OperationBit[3] = false;
       OperationBit[2] = true;
       OperationBit[1] = true;
       OperationBit[0] = false;
     }else
     /* 10 XX0100 = 0000 */
     if (alu1 && !alu0 && !bit[3] && bit[2] && !bit[1] && !bit[0] ) {
       OperationBit[3] = false;
       OperationBit[2] = false;
       OperationBit[1] = false;
       OperationBit[0] = false;
     }else
     /* 10 XX0101 =  0001 */
     if (alu1 && !alu0 && !bit[3] && bit[2] && !bit[1] && bit[0] ) {
       OperationBit[3] = false;
       OperationBit[2] = false;
       OperationBit[1] = false;
       OperationBit[0] = true;
     }else
     /* 1X XX1010 = 0111 */
     if (alu1 && bit[3] && !bit[2] && bit[1] && !bit[0] ) {
       OperationBit[3] = false;
       OperationBit[2] = true;
       OperationBit[1] = true;
       OperationBit[0] = true;
     }
     //TODO: NOR?

    return Operation.getFunction(OperationBit);

  }
}
