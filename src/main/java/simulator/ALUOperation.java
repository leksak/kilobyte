package simulator;

public class ALUOperation {
  DataMemory dataMemory;
  private boolean alu0, alu1;

  public ALUOperation(DataMemory dataMemory) {
    this.dataMemory = dataMemory;
  }

  public void ALUOp0(boolean alu0) {
    this.alu0 = alu0;
  }

  public void ALUOp1(boolean alu1) {
    this.alu1 = alu1;
  }

  public void functionCode(int ret5to0) {
    boolean[] bit = new boolean[6];
    boolean[] OperationBit = new boolean[4];
    for (int i = 5; i >= 0; i--) {
      bit[i] = (ret5to0 & (1 << i)) != 0;
    }

    /* alu0-alu1 f5-f4-f3-f-2-f1-f0 == Operationbits*/
    /* 00 11111 == 0010 */
    if (!alu0 && !alu1 && bit[5] && bit[4] && bit[3] && bit[2] && bit[1] && bit[0] ) {
      OperationBit[3] = false;
      OperationBit[2] = false;
      OperationBit[1] = false;
      OperationBit[0] = false;


    }

    /**
     * 11 111111
     if (alu0 && alu1 && bit[5] && bit[4] && bit[3] && bit[2] && bit[1] && bit[0] ) {
       OperationBit[3] = false;
       OperationBit[2] = false;
       OperationBit[1] = false;
       OperationBit[0] = false;
     }
     */


  }
}
